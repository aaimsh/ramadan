package com.ramadan.iftartracker.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ramadan.iftartracker.IftarTrackerApp
import com.ramadan.iftartracker.R
import com.ramadan.iftartracker.data.prayer.PrayerTimeCalculator
import com.ramadan.iftartracker.domain.model.CalculationMethod
import com.ramadan.iftartracker.domain.model.Coordinates
import com.ramadan.iftartracker.domain.usecase.GetCountdownUseCase
import com.ramadan.iftartracker.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class CountdownService : Service() {

    @Inject lateinit var calculator: PrayerTimeCalculator
    @Inject lateinit var getCountdown: GetCountdownUseCase

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val lat = intent?.getDoubleExtra(EXTRA_LAT, 0.0) ?: 0.0
        val lng = intent?.getDoubleExtra(EXTRA_LNG, 0.0) ?: 0.0

        startForeground(NOTIFICATION_ID, buildNotification("Calculating..."))

        scope.launch {
            val prayerTimes = calculator.calculate(
                date = LocalDate.now(),
                coordinates = Coordinates(lat, lng),
                method = CalculationMethod.MWL,
            )

            while (isActive) {
                val countdown = getCountdown(prayerTimes, LocalDateTime.now())
                val text = "${countdown.targetName} in ${countdown.displayTime}"
                val notification = buildNotification(text)
                val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
                manager.notify(NOTIFICATION_ID, notification)
                delay(1000)
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun buildNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, IftarTrackerApp.CHANNEL_COUNTDOWN)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Iftar Tracker")
            .setContentText(text)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val EXTRA_LAT = "extra_lat"
        const val EXTRA_LNG = "extra_lng"
    }
}

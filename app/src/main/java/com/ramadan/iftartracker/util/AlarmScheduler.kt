package com.ramadan.iftartracker.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.ramadan.iftartracker.data.location.LocationProvider
import com.ramadan.iftartracker.data.prayer.PrayerTimeCalculator
import com.ramadan.iftartracker.data.preferences.UserPreferences
import com.ramadan.iftartracker.receiver.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val calculator: PrayerTimeCalculator,
    private val locationProvider: LocationProvider,
    private val preferences: UserPreferences,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun rescheduleAlarms() {
        CoroutineScope(Dispatchers.IO).launch {
            val coords = locationProvider.getCoordinates() ?: return@launch
            val method = preferences.calculationMethod.first()
            val prayerTimes = calculator.calculate(LocalDate.now(), coords, method)
            val iftarMinutes = preferences.iftarAlertMinutes.first()
            val suhoorMinutes = preferences.suhoorAlertMinutes.first()

            // Schedule Iftar exact alert
            scheduleAlarm(
                time = prayerTimes.maghrib,
                type = AlarmReceiver.ALERT_IFTAR,
                requestCode = 100,
            )

            // Schedule Iftar pre-alert
            scheduleAlarm(
                time = prayerTimes.maghrib.minusMinutes(iftarMinutes.toLong()),
                type = AlarmReceiver.ALERT_IFTAR_SOON,
                requestCode = 101,
                extraMinutes = iftarMinutes,
            )

            // Schedule Suhoor pre-alert
            scheduleAlarm(
                time = prayerTimes.fajr.minusMinutes(suhoorMinutes.toLong()),
                type = AlarmReceiver.ALERT_SUHOOR,
                requestCode = 102,
                extraMinutes = suhoorMinutes,
            )
        }
    }

    private fun scheduleAlarm(
        time: LocalTime,
        type: String,
        requestCode: Int,
        extraMinutes: Int = 0,
    ) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_ALERT_TYPE, type)
            if (extraMinutes > 0) {
                putExtra(AlarmReceiver.EXTRA_MINUTES, extraMinutes)
            }
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val today = LocalDate.now()
        val triggerTime = time.atDate(today)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val now = System.currentTimeMillis()
        if (triggerTime > now) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent,
            )
        }
    }
}

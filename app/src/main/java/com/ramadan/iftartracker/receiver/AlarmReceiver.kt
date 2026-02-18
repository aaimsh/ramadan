package com.ramadan.iftartracker.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.ramadan.iftartracker.IftarTrackerApp
import com.ramadan.iftartracker.R
import com.ramadan.iftartracker.ui.MainActivity

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_ALERT_TYPE) ?: return
        val title: String
        val message: String

        when (type) {
            ALERT_IFTAR -> {
                title = "Iftar Time"
                message = "It's time to break your fast!"
            }
            ALERT_IFTAR_SOON -> {
                val minutes = intent.getIntExtra(EXTRA_MINUTES, 15)
                title = "Iftar Soon"
                message = "Iftar is in $minutes minutes. Prepare to break your fast."
            }
            ALERT_SUHOOR -> {
                val minutes = intent.getIntExtra(EXTRA_MINUTES, 30)
                title = "Suhoor Reminder"
                message = "Suhoor ends in $minutes minutes."
            }
            else -> return
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, IftarTrackerApp.CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as android.app.NotificationManager
        manager.notify(type.hashCode(), notification)
    }

    companion object {
        const val EXTRA_ALERT_TYPE = "alert_type"
        const val EXTRA_MINUTES = "minutes"
        const val ALERT_IFTAR = "iftar"
        const val ALERT_IFTAR_SOON = "iftar_soon"
        const val ALERT_SUHOOR = "suhoor"
    }
}

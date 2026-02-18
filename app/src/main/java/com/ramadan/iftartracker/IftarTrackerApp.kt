package com.ramadan.iftartracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class IftarTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val countdownChannel = NotificationChannel(
            CHANNEL_COUNTDOWN,
            "Iftar Countdown",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Shows the live countdown to Iftar"
        }

        val alertChannel = NotificationChannel(
            CHANNEL_ALERTS,
            "Prayer Alerts",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Alerts for Iftar and Suhoor times"
            enableVibration(true)
        }

        manager.createNotificationChannel(countdownChannel)
        manager.createNotificationChannel(alertChannel)
    }

    companion object {
        const val CHANNEL_COUNTDOWN = "countdown"
        const val CHANNEL_ALERTS = "alerts"
    }
}

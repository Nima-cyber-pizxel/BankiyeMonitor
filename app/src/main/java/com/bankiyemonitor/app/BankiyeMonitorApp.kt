package com.bankiyemonitor.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BankiyeMonitorApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val alertChannel = NotificationChannel(
            CHANNEL_SECURITY_ALERTS,
            getString(R.string.notification_channel_alerts),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts for suspicious banking activity, spoofed messages, and unauthorized access attempts"
            enableVibration(true)
            enableLights(true)
        }

        val summaryChannel = NotificationChannel(
            CHANNEL_SUMMARIES,
            getString(R.string.notification_channel_summaries),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Periodic account activity summaries (daily, weekly, monthly)"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannels(listOf(alertChannel, summaryChannel))
    }

    companion object {
        const val CHANNEL_SECURITY_ALERTS = "security_alerts"
        const val CHANNEL_SUMMARIES = "account_summaries"
    }
}

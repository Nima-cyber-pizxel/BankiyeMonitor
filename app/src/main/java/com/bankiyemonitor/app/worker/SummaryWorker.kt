package com.bankiyemonitor.app.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bankiyemonitor.app.BankiyeMonitorApp
import com.bankiyemonitor.app.data.local.dao.ParsedSmsDao
import com.bankiyemonitor.app.data.local.dao.TransactionDao
import com.bankiyemonitor.app.data.local.entity.TransactionSummaryEntity
import com.bankiyemonitor.app.ui.MainActivity
import com.bankiyemonitor.app.ui.components.formatEtb
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.ZoneId

@HiltWorker
class SummaryWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val parsedSmsDao: ParsedSmsDao,
    private val transactionDao: TransactionDao
) : CoroutineWorker(appContext, workerParams) {

    private val gson = Gson()

    override suspend fun doWork(): Result {
        return try {
            val periodType = inputData.getString("PERIOD_TYPE") ?: "WEEKLY"
            val now = LocalDate.now()
            val periodStart = when (periodType) {
                "DAILY" -> now
                "WEEKLY" -> now.minusDays((now.dayOfWeek.value - 1).toLong())
                "MONTHLY" -> now.withDayOfMonth(1)
                "YEARLY" -> now.withDayOfYear(1)
                else -> now.minusDays(7)
            }

            val startTime = periodStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endTime = System.currentTimeMillis()

            val totalSent = parsedSmsDao.getTotalDebited(startTime, endTime) ?: 0.0
            val totalReceived = parsedSmsDao.getTotalCredited(startTime, endTime) ?: 0.0
            val loginAttempts = parsedSmsDao.getLoginCount(startTime, endTime)
            val failedLogins = parsedSmsDao.getFailedLoginCount(startTime, endTime)
            val passwordResets = parsedSmsDao.getPasswordResetCount(startTime, endTime)
            val deviceChanges = parsedSmsDao.getDeviceChangeCount(startTime, endTime)
            val spoofedMessages = parsedSmsDao.getSpoofedCount(startTime, endTime)
            val highRiskEvents = parsedSmsDao.getHighRiskCount(startTime, endTime)
            val totalCount = parsedSmsDao.getTotalCount()

            val summary = TransactionSummaryEntity(
                periodType = periodType,
                periodStart = startTime,
                periodEnd = endTime,
                totalSent = totalSent,
                totalReceived = totalReceived,
                netFlow = totalReceived - totalSent,
                transactionCount = totalCount,
                loginAttempts = loginAttempts,
                failedLogins = failedLogins,
                passwordResets = passwordResets,
                deviceChanges = deviceChanges,
                spoofedMessages = spoofedMessages,
                highRiskEvents = highRiskEvents,
                generatedAt = System.currentTimeMillis()
            )

            transactionDao.insert(summary)

            // Post notification for user
            showSummaryNotification(periodType, totalReceived, totalSent, failedLogins + spoofedMessages)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun showSummaryNotification(
        periodType: String,
        received: Double,
        sent: Double,
        securityAlerts: Int
    ) {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(appContext, 1, intent, flags)

        val title = "BankiyeMonitor $periodType Executive Summary"
        val body = "Received: ${formatEtb(received)} | Sent: ${formatEtb(sent)} | Security warnings: $securityAlerts"

        val notification = NotificationCompat.Builder(appContext, BankiyeMonitorApp.CHANNEL_SUMMARIES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(2001, notification)
    }
}

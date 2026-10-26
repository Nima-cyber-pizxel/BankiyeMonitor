package com.bankiyemonitor.app.sms

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.bankiyemonitor.app.BankiyeMonitorApp
import com.bankiyemonitor.app.data.local.dao.ParsedSmsDao
import com.bankiyemonitor.app.data.local.dao.SecurityEventDao
import com.bankiyemonitor.app.data.local.entity.SecurityEventEntity
import com.bankiyemonitor.app.data.local.entity.SecurityEventType
import com.bankiyemonitor.app.data.repository.SmsRepository
import com.bankiyemonitor.app.parser.BankSmsParser
import com.bankiyemonitor.app.parser.model.RiskLevel
import com.bankiyemonitor.app.parser.model.TransactionType
import com.bankiyemonitor.app.parser.model.VerificationStatus
import com.bankiyemonitor.app.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var smsRepository: SmsRepository

    @Inject
    lateinit var bankSmsParser: BankSmsParser

    @Inject
    lateinit var securityEventDao: SecurityEventDao

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val pendingResult = goAsync()

        receiverScope.launch {
            try {
                // Combine multipart SMS segments by sender
                val grouped = messages.groupBy { it.originatingAddress ?: "Unknown" }
                
                for ((sender, parts) in grouped) {
                    val fullBody = parts.joinToString(separator = "") { it.messageBody ?: "" }
                    if (fullBody.isBlank()) continue

                    val savedEntity = smsRepository.parseAndSave(sender, fullBody) ?: continue

                    // Check if a security event should be recorded and notified
                    handleSecurityEvent(context, savedEntity.id, sender, fullBody)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleSecurityEvent(
        context: Context,
        smsId: Long,
        sender: String,
        body: String
    ) {
        val parsed = bankSmsParser.parse(sender, body)

        var eventType: SecurityEventType? = null
        var alertTitle: String? = null
        var alertDesc: String? = null

        when {
            parsed.verificationStatus == VerificationStatus.SPOOFED -> {
                eventType = SecurityEventType.SPOOF_DETECTED
                alertTitle = "Potential Bank Spoof Detected!"
                alertDesc = "Sender '$sender' claimed to be ${parsed.bankName} but is not on Wolaita Sodo verified bank whitelist."
            }
            parsed.transactionType == TransactionType.LOGIN_FAILED -> {
                eventType = SecurityEventType.FAILED_LOGIN
                alertTitle = "Failed Login Attempt Alert"
                alertDesc = "Failed login detected on your ${parsed.bankName} account."
            }
            parsed.transactionType == TransactionType.PASSWORD_RESET -> {
                eventType = SecurityEventType.PASSWORD_RESET
                alertTitle = "Password Reset Initiated"
                alertDesc = "Password reset request detected on ${parsed.bankName}."
            }
            parsed.transactionType == TransactionType.DEVICE_CHANGE -> {
                eventType = SecurityEventType.UNKNOWN_DEVICE
                alertTitle = "New Device / Session Detected"
                alertDesc = "New device or session detected on ${parsed.bankName}: ${parsed.deviceInfo ?: "Unknown"}"
            }
            parsed.containsLink && parsed.verificationStatus != VerificationStatus.VERIFIED -> {
                eventType = SecurityEventType.PHISHING_ATTEMPT
                alertTitle = "Suspicious Link / Phishing Warning"
                alertDesc = "Message claiming bank affiliation contains external links."
            }
            parsed.riskLevel == RiskLevel.HIGH || parsed.riskLevel == RiskLevel.CRITICAL -> {
                eventType = SecurityEventType.HIGH_URGENCY_MESSAGE
                alertTitle = "High Risk Alert: ${parsed.bankName}"
                alertDesc = parsed.riskReasons.firstOrNull() ?: "Urgent suspicious language detected."
            }
        }

        if (eventType != null && alertTitle != null && alertDesc != null) {
            val eventEntity = SecurityEventEntity(
                smsId = smsId,
                eventType = eventType.name,
                riskLevel = parsed.riskLevel.name,
                title = alertTitle,
                description = alertDesc,
                bankName = parsed.bankName,
                senderAddress = sender,
                timestamp = System.currentTimeMillis(),
                deviceInfo = parsed.deviceInfo
            )
            securityEventDao.insert(eventEntity)
            showNotification(context, alertTitle, alertDesc)
        }
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, flags)

        val notification = NotificationCompat.Builder(context, BankiyeMonitorApp.CHANNEL_SECURITY_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

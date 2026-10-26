package com.bankiyemonitor.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "security_events",
    indices = [
        Index(value = ["eventType"]),
        Index(value = ["riskLevel"]),
        Index(value = ["timestamp"]),
        Index(value = ["isResolved"])
    ]
)
data class SecurityEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val smsId: Long,             // Reference to parsed_sms
    val eventType: String,       // SecurityEventType enum name
    val riskLevel: String,       // RiskLevel enum name
    val title: String,
    val description: String,
    val bankName: String,
    val senderAddress: String,
    val timestamp: Long,         // Epoch millis
    val deviceInfo: String? = null,
    val ipAddress: String? = null,
    val isResolved: Boolean = false,
    val resolvedAt: Long? = null,
    val userNotes: String? = null
)

enum class SecurityEventType {
    SPOOF_DETECTED,
    FAILED_LOGIN,
    UNKNOWN_DEVICE,
    PASSWORD_RESET,
    PHISHING_ATTEMPT,
    SUSPICIOUS_OTP,
    ACCOUNT_LOCKED,
    HIGH_URGENCY_MESSAGE,
    UNUSUAL_TRANSACTION
}

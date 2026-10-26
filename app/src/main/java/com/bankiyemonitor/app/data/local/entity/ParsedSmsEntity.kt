package com.bankiyemonitor.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "parsed_sms",
    indices = [
        Index(value = ["senderAddress"]),
        Index(value = ["transactionType"]),
        Index(value = ["bankName"]),
        Index(value = ["timestamp"]),
        Index(value = ["verificationStatus"]),
        Index(value = ["riskLevel"])
    ]
)
data class ParsedSmsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rawMessage: String,
    val senderAddress: String,
    val transactionType: String,  // TransactionType enum name
    val amount: Double? = null,
    val currency: String = "ETB",
    val counterparty: String? = null,
    val accountNumber: String? = null,
    val referenceNumber: String? = null,
    val balance: Double? = null,
    val timestamp: Long,          // Epoch millis
    val parsedAt: Long,           // Epoch millis
    val bankName: String,
    val verificationStatus: String,  // VerificationStatus enum name
    val riskLevel: String,           // RiskLevel enum name
    val riskReasons: String = "",    // JSON array of strings
    val deviceInfo: String? = null,
    val sessionInfo: String? = null,
    val otpDetected: Boolean = false,
    val urgencyScore: Int = 0,
    val containsLink: Boolean = false,
    val isRead: Boolean = false,
    val isFlagged: Boolean = false
)

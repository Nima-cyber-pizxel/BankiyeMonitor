package com.bankiyemonitor.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transaction_summaries",
    indices = [
        Index(value = ["periodType", "periodStart"], unique = true),
        Index(value = ["bankName"])
    ]
)
data class TransactionSummaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val periodType: String,      // DAILY, WEEKLY, MONTHLY, YEARLY
    val periodStart: Long,       // Epoch millis - start of period
    val periodEnd: Long,         // Epoch millis - end of period
    val bankName: String? = null, // null = all banks
    val totalSent: Double = 0.0,
    val totalReceived: Double = 0.0,
    val netFlow: Double = 0.0,   // received - sent
    val transactionCount: Int = 0,
    val creditCount: Int = 0,
    val debitCount: Int = 0,
    val loginAttempts: Int = 0,
    val failedLogins: Int = 0,
    val passwordResets: Int = 0,
    val deviceChanges: Int = 0,
    val spoofedMessages: Int = 0,
    val highRiskEvents: Int = 0,
    val uniqueCounterparties: Int = 0,
    val counterpartiesSentTo: String = "", // JSON array
    val counterpartiesReceivedFrom: String = "", // JSON array
    val generatedAt: Long         // Epoch millis
)

enum class SummaryPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

package com.bankiyemonitor.app.data.sample

import com.bankiyemonitor.app.data.local.dao.ParsedSmsDao
import com.bankiyemonitor.app.data.local.dao.SecurityEventDao
import com.bankiyemonitor.app.data.local.entity.ParsedSmsEntity
import com.bankiyemonitor.app.data.local.entity.SecurityEventEntity
import com.bankiyemonitor.app.data.local.entity.SecurityEventType
import com.bankiyemonitor.app.parser.model.RiskLevel
import com.bankiyemonitor.app.parser.model.TransactionType
import com.bankiyemonitor.app.parser.model.VerificationStatus
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SampleDataSeeder @Inject constructor(
    private val parsedSmsDao: ParsedSmsDao,
    private val securityEventDao: SecurityEventDao
) {
    private val gson = Gson()

    suspend fun seedSampleData() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val oneHourAgo = now - 3_600_000L
        val fiveHoursAgo = now - 18_000_000L
        val oneDayAgo = now - 86_400_000L
        val twoDaysAgo = now - 172_800_000L
        val threeDaysAgo = now - 259_200_000L

        val sampleMessages = listOf(
            // 1. CBE Credit Alert (Legitimate)
            ParsedSmsEntity(
                rawMessage = "Dear Customer, Your account 100012345678 has been credited with ETB 18,500.00 from WOLAITA AGRICULTURAL COOPERATIVE on 25/10/2026. Avail Bal: ETB 45,200.75. Ref: TXN987654321.",
                senderAddress = "CBE",
                transactionType = TransactionType.CREDIT.name,
                amount = 18500.00,
                currency = "ETB",
                counterparty = "WOLAITA AGRICULTURAL COOPERATIVE",
                accountNumber = "5678",
                referenceNumber = "TXN987654321",
                balance = 45200.75,
                timestamp = oneHourAgo,
                parsedAt = now,
                bankName = "Commercial Bank of Ethiopia",
                verificationStatus = VerificationStatus.VERIFIED.name,
                riskLevel = RiskLevel.LOW.name,
                riskReasons = gson.toJson(listOf<String>()),
                urgencyScore = 0
            ),

            // 2. Dashen Debit Alert (Legitimate)
            ParsedSmsEntity(
                rawMessage = "Your account 0023456789 was debited with ETB 3,450.00 for payment to Sodo Central Supermarket on 24/10/2026. Avail Bal: ETB 41,750.75. Ref: DSH88776655.",
                senderAddress = "DashenBank",
                transactionType = TransactionType.DEBIT.name,
                amount = 3450.00,
                currency = "ETB",
                counterparty = "Sodo Central Supermarket",
                accountNumber = "6789",
                referenceNumber = "DSH88776655",
                balance = 41750.75,
                timestamp = fiveHoursAgo,
                parsedAt = now,
                bankName = "Dashen Bank",
                verificationStatus = VerificationStatus.VERIFIED.name,
                riskLevel = RiskLevel.LOW.name,
                riskReasons = gson.toJson(listOf<String>()),
                urgencyScore = 0
            ),

            // 3. Bank of Abyssinia Failed Login (Security Alert)
            ParsedSmsEntity(
                rawMessage = "BOA Alert: 3 failed login attempts detected on your mobile banking account from unrecognized device ID: SM-G998B in Addis Ababa. If this was not you, change password.",
                senderAddress = "BOA",
                transactionType = TransactionType.LOGIN_FAILED.name,
                amount = null,
                currency = "ETB",
                counterparty = null,
                accountNumber = null,
                referenceNumber = null,
                balance = null,
                timestamp = oneDayAgo,
                parsedAt = now,
                bankName = "Bank of Abyssinia",
                verificationStatus = VerificationStatus.VERIFIED.name,
                riskLevel = RiskLevel.HIGH.name,
                riskReasons = gson.toJson(listOf("Failed login attempts from unrecognized device")),
                deviceInfo = "SM-G998B",
                urgencyScore = 45
            ),

            // 4. Critical Spoofed SMS (Impersonation Attack)
            ParsedSmsEntity(
                rawMessage = "CBE-Notice: Your Commercial Bank account is suspended due to abnormal transactions. Verify your identity immediately at http://cbe-ethiopia-auth.tk within 24 hours to restore access.",
                senderAddress = "+251911998877",
                transactionType = TransactionType.UNKNOWN.name,
                amount = null,
                currency = "ETB",
                counterparty = null,
                accountNumber = null,
                referenceNumber = null,
                balance = null,
                timestamp = twoDaysAgo,
                parsedAt = now,
                bankName = "Commercial Bank of Ethiopia",
                verificationStatus = VerificationStatus.SPOOFED.name,
                riskLevel = RiskLevel.CRITICAL.name,
                riskReasons = gson.toJson(listOf(
                    "Sender '+251911998877' is an unauthorized phone number mimicking CBE",
                    "Message contains phishing link: http://cbe-ethiopia-auth.tk",
                    "High urgency language detected ('suspended', 'immediately', 'within 24 hours')"
                )),
                urgencyScore = 85,
                containsLink = true
            ),

            // 5. Awash Bank Password Reset & OTP (Legitimate Security Event)
            ParsedSmsEntity(
                rawMessage = "Your AwashBank password reset request was initiated. OTP code is 934812. Valid for 5 minutes. NEVER share your OTP with anyone.",
                senderAddress = "AwashBank",
                transactionType = TransactionType.PASSWORD_RESET.name,
                amount = null,
                currency = "ETB",
                counterparty = null,
                accountNumber = null,
                referenceNumber = null,
                balance = null,
                timestamp = threeDaysAgo,
                parsedAt = now,
                bankName = "Awash Bank",
                verificationStatus = VerificationStatus.VERIFIED.name,
                riskLevel = RiskLevel.MEDIUM.name,
                riskReasons = gson.toJson(listOf("Password reset request")),
                otpDetected = true,
                urgencyScore = 20
            )
        )

        val insertedIds = parsedSmsDao.insertAll(sampleMessages)

        // Add corresponding security events
        if (insertedIds.size >= 4) {
            val failedLoginSmsId = insertedIds[2]
            val spoofedSmsId = insertedIds[3]

            securityEventDao.insert(
                SecurityEventEntity(
                    smsId = failedLoginSmsId,
                    eventType = SecurityEventType.FAILED_LOGIN.name,
                    riskLevel = RiskLevel.HIGH.name,
                    title = "Failed Login Attempts Detected",
                    description = "Multiple failed login attempts on Bank of Abyssinia from device SM-G998B.",
                    bankName = "Bank of Abyssinia",
                    senderAddress = "BOA",
                    timestamp = oneDayAgo,
                    deviceInfo = "SM-G998B",
                    isResolved = false
                )
            )

            securityEventDao.insert(
                SecurityEventEntity(
                    smsId = spoofedSmsId,
                    eventType = SecurityEventType.SPOOF_DETECTED.name,
                    riskLevel = RiskLevel.CRITICAL.name,
                    title = "⚠️ Fake CBE SMS / Phishing Attack",
                    description = "Unauthorized number +251911998877 impersonated Commercial Bank of Ethiopia with malicious link.",
                    bankName = "Commercial Bank of Ethiopia",
                    senderAddress = "+251911998877",
                    timestamp = twoDaysAgo,
                    isResolved = false
                )
            )
        }
    }
}

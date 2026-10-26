package com.bankiyemonitor.app.parser.model

import java.time.LocalDateTime

/**
 * The structured output of the SMS parser.
 * Transforms raw unstructured bank SMS into clean, typed data.
 */
data class ParsedSmsData(
    val rawMessage: String,
    val senderAddress: String,
    val transactionType: TransactionType,
    val amount: Double? = null,
    val currency: String = "ETB",
    val counterparty: String? = null,
    val accountNumber: String? = null,
    val referenceNumber: String? = null,
    val balance: Double? = null,
    val timestamp: LocalDateTime,
    val parsedAt: LocalDateTime = LocalDateTime.now(),
    val bankName: String,
    val verificationStatus: VerificationStatus,
    val riskLevel: RiskLevel,
    val riskReasons: List<String> = emptyList(),
    val deviceInfo: String? = null,
    val sessionInfo: String? = null,
    val otpDetected: Boolean = false,
    val urgencyScore: Int = 0, // 0-100
    val containsLink: Boolean = false,
    val rawTokens: List<SmsToken> = emptyList()
)

enum class TransactionType {
    CREDIT,          // Money received
    DEBIT,           // Money sent
    LOGIN,           // Account login
    LOGIN_FAILED,    // Failed login attempt
    PASSWORD_RESET,  // Password reset request
    DEVICE_CHANGE,   // New device/session
    OTP,             // One-time password
    BALANCE_INQUIRY, // Balance check
    ACCOUNT_LOCKED,  // Account lockout
    BILL_PAYMENT,    // Bill/utility payment
    AIRTIME,         // Airtime purchase
    UNKNOWN          // Unrecognized type
}

enum class VerificationStatus {
    VERIFIED,    // Sender matches bank whitelist
    UNVERIFIED,  // Sender not in whitelist (may or may not be legitimate)
    SPOOFED      // Sender appears to be impersonating a bank
}

enum class RiskLevel {
    LOW,     // Normal transaction
    MEDIUM,  // Unusual but not necessarily malicious
    HIGH,    // Suspicious, requires attention
    CRITICAL // Likely attack or fraud attempt
}

/**
 * Individual token extracted during lexical analysis
 */
data class SmsToken(
    val type: TokenType,
    val value: String,
    val position: IntRange
)

enum class TokenType {
    AMOUNT,
    CURRENCY,
    ACCOUNT_NUMBER,
    REFERENCE,
    DATE,
    TIME,
    PHONE_NUMBER,
    DEVICE_ID,
    OTP_CODE,
    URL,
    KEYWORD,
    TEXT
}

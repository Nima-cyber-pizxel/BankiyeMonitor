package com.bankiyemonitor.app.parser

import com.bankiyemonitor.app.parser.model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * The main SMS Parser Engine - the intelligence core of BankiyeMonitor.
 * 
 * This parser reads, tokenizes, and categorizes every incoming bank alert.
 * By applying strict grammar rules (lexical analysis) to the SMS text,
 * the parser extracts structured data fields.
 * 
 * It handles:
 * 1. Spoof Detection & Sender Verification
 * 2. Unstructured → Structured Data Transformation
 * 3. Transaction Type Classification
 * 4. Adaptive Bank Format Handling
 * 5. Social Engineering Detection
 */
class BankSmsParser(
    private val bankFormatRegistry: BankFormatRegistry,
    private val spoofDetector: SpoofDetector
) {
    private val tokenizer = SmsTokenizer()

    /**
     * Parse a raw SMS message into structured data.
     * This is the main entry point for the parsing engine.
     */
    fun parse(senderAddress: String, messageBody: String, receivedAt: LocalDateTime = LocalDateTime.now()): ParsedSmsData {
        // Step 1: Spoof analysis
        val spoofAnalysis = spoofDetector.analyze(senderAddress, messageBody)
        
        // Step 2: Tokenize the message
        val tokens = tokenizer.tokenize(messageBody)
        
        // Step 3: Determine the bank format to use
        val bankFormat = spoofAnalysis.matchedBank
            ?: bankFormatRegistry.findBankBySender(senderAddress)
        
        val bankName = bankFormat?.bankName ?: identifyBankFromContent(messageBody)
        
        // Step 4: Determine transaction type
        val transactionType = classifyTransactionType(messageBody, bankFormat)
        
        // Step 5: Extract structured data fields
        val amount = extractAmount(messageBody, bankFormat)
        val balance = extractBalance(messageBody, bankFormat)
        val accountNumber = extractAccountNumber(messageBody, bankFormat)
        val referenceNumber = extractReference(messageBody, bankFormat)
        val counterparty = extractCounterparty(messageBody, bankFormat)
        val timestamp = extractTimestamp(messageBody, bankFormat) ?: receivedAt
        val deviceInfo = extractDeviceInfo(messageBody, bankFormat)
        val otpDetected = detectOtp(messageBody, bankFormat)

        return ParsedSmsData(
            rawMessage = messageBody,
            senderAddress = senderAddress,
            transactionType = transactionType,
            amount = amount,
            currency = "ETB",
            counterparty = counterparty,
            accountNumber = accountNumber,
            referenceNumber = referenceNumber,
            balance = balance,
            timestamp = timestamp,
            bankName = bankName,
            verificationStatus = spoofAnalysis.verificationStatus,
            riskLevel = spoofAnalysis.riskLevel,
            riskReasons = spoofAnalysis.reasons,
            deviceInfo = deviceInfo,
            otpDetected = otpDetected,
            urgencyScore = spoofAnalysis.urgencyScore,
            containsLink = spoofAnalysis.containsLink,
            rawTokens = tokens
        )
    }

    /**
     * Classify the transaction type based on message content and bank-specific patterns.
     */
    private fun classifyTransactionType(message: String, bankFormat: BankFormat?): TransactionType {
        // Try bank-specific patterns first, then fall back to generic
        val patterns = bankFormat?.let { format ->
            mapOf(
                TransactionType.LOGIN_FAILED to format.failedLoginPatterns,
                TransactionType.PASSWORD_RESET to format.passwordResetPatterns,
                TransactionType.DEVICE_CHANGE to format.deviceChangePatterns,
                TransactionType.OTP to format.otpPatterns,
                TransactionType.LOGIN to format.loginPatterns,
                TransactionType.CREDIT to format.creditPatterns,
                TransactionType.DEBIT to format.debitPatterns
            )
        } ?: getGenericPatterns()

        // Check patterns in priority order (security events first)
        for ((type, typePatterns) in patterns) {
            for (pattern in typePatterns) {
                if (Regex(pattern).containsMatchIn(message)) {
                    return type
                }
            }
        }

        // Additional generic classification
        val lowerMessage = message.lowercase()
        return when {
            lowerMessage.contains("account locked") || lowerMessage.contains("account suspended") -> TransactionType.ACCOUNT_LOCKED
            lowerMessage.contains("balance") && !lowerMessage.contains(Regex("debit|credit|transfer")) -> TransactionType.BALANCE_INQUIRY
            lowerMessage.contains("airtime") || lowerMessage.contains("recharge") -> TransactionType.AIRTIME
            lowerMessage.contains(Regex("(?i)bill|utility|electric|water|telecom")) -> TransactionType.BILL_PAYMENT
            else -> TransactionType.UNKNOWN
        }
    }

    /**
     * Extract the transaction amount from the message.
     */
    private fun extractAmount(message: String, bankFormat: BankFormat?): Double? {
        val patterns = bankFormat?.let { 
            it.creditPatterns + it.debitPatterns 
        } ?: listOf(
            """(?i)(?:ETB|Birr)\s*([\d,]+\.?\d*)""",
            """(?i)([\d,]+\.?\d*)\s*(?:ETB|Birr)""",
            """(?i)(?:amount|amt)\s*[:=]?\s*(?:ETB|Birr)?\s*([\d,]+\.?\d*)"""
        )

        for (pattern in patterns) {
            val match = Regex(pattern).find(message)
            if (match != null) {
                val amountStr = match.groupValues.lastOrNull { it.isNotBlank() && it.matches(Regex("[\\d,]+\\.?\\d*")) }
                    ?: continue
                return parseAmount(amountStr, bankFormat)
            }
        }
        return null
    }

    /**
     * Extract balance from the message.
     */
    private fun extractBalance(message: String, bankFormat: BankFormat?): Double? {
        val patterns = bankFormat?.balancePatterns ?: listOf(
            """(?i)(?:balance|bal|avl\.?\s*bal|available)\s*(?:is|:)?\s*(?:ETB|Birr)?\s*([\d,]+\.?\d*)"""
        )

        for (pattern in patterns) {
            val match = Regex(pattern).find(message)
            if (match != null) {
                val amountStr = match.groupValues.lastOrNull { it.isNotBlank() && it.matches(Regex("[\\d,]+\\.?\\d*")) }
                    ?: continue
                return parseAmount(amountStr, bankFormat)
            }
        }
        return null
    }

    /**
     * Parse amount string handling thousand separators and decimals.
     */
    private fun parseAmount(amountStr: String, bankFormat: BankFormat?): Double? {
        return try {
            val separator = bankFormat?.amountThousandSeparator ?: ","
            amountStr.replace(separator, "").toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extract account number from the message.
     */
    private fun extractAccountNumber(message: String, bankFormat: BankFormat?): String? {
        val patterns = bankFormat?.accountPatterns ?: listOf(
            """(?i)(?:a/?c|account|acct)\s*(?:no\.?|number|#)?\s*[:=]?\s*[*xX]*(\d{4,})"""
        )

        for (pattern in patterns) {
            val match = Regex(pattern).find(message)
            if (match != null) {
                return match.groupValues.lastOrNull { it.isNotBlank() && it.matches(Regex("\\d+")) }
            }
        }
        return null
    }

    /**
     * Extract transaction reference number.
     */
    private fun extractReference(message: String, bankFormat: BankFormat?): String? {
        val patterns = bankFormat?.referencePatterns ?: listOf(
            """(?i)(?:ref|reference|txn|transaction)\s*(?:no\.?|number|#|ID)?\s*[:=]?\s*([A-Za-z0-9]+)"""
        )

        for (pattern in patterns) {
            val match = Regex(pattern).find(message)
            if (match != null) {
                return match.groupValues.lastOrNull { it.isNotBlank() }
            }
        }
        return null
    }

    /**
     * Extract counterparty (who you sent to or received from).
     */
    private fun extractCounterparty(message: String, bankFormat: BankFormat?): String? {
        val patterns = bankFormat?.counterpartyPatterns ?: listOf(
            """(?i)(?:from|to|sent to|received from|paid to|transferred to)\s+([A-Za-z\s]+?)(?:\s+(?:on|at|ref|ETB|Birr|\d)|$)"""
        )

        for (pattern in patterns) {
            val match = Regex(pattern).find(message)
            if (match != null) {
                val counterparty = match.groupValues.lastOrNull { it.isNotBlank() }?.trim()
                if (counterparty != null && counterparty.length > 1) {
                    return counterparty
                }
            }
        }
        return null
    }

    /**
     * Extract and parse timestamp from the message.
     */
    private fun extractTimestamp(message: String, bankFormat: BankFormat?): LocalDateTime? {
        val patterns = bankFormat?.timestampPatterns ?: listOf(
            """(\d{1,2}[/\-]\d{1,2}[/\-]\d{2,4}\s+\d{1,2}:\d{2}(?::\d{2})?\s*(?:AM|PM)?)""",
            """(\d{1,2}[/\-]\d{1,2}[/\-]\d{2,4})"""
        )

        val dateTimeFormats = listOf(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a"),
            DateTimeFormatter.ofPattern("dd/MM/yy HH:mm")
        )

        for (pattern in patterns) {
            val match = Regex(pattern).find(message) ?: continue
            val dateStr = match.groupValues[1]
            
            for (format in dateTimeFormats) {
                try {
                    return LocalDateTime.parse(dateStr, format)
                } catch (_: DateTimeParseException) {
                    continue
                }
            }
        }
        return null
    }

    /**
     * Extract device/session information from security messages.
     */
    private fun extractDeviceInfo(message: String, bankFormat: BankFormat?): String? {
        val patterns = bankFormat?.deviceChangePatterns ?: listOf(
            """(?i)(?:device|session)\s*(?:ID|token)\s*[:=]?\s*(\S+)""",
            """(?i)(?:from|using)\s+(?:device|browser|phone)\s*[:=]?\s*(.+?)(?:\.|$)"""
        )

        for (pattern in patterns) {
            val match = Regex(pattern).find(message)
            if (match != null) {
                return match.groupValues.lastOrNull { it.isNotBlank() }
            }
        }
        return null
    }

    /**
     * Detect if message contains OTP/verification code.
     */
    private fun detectOtp(message: String, bankFormat: BankFormat?): Boolean {
        val patterns = bankFormat?.otpPatterns ?: listOf(
            """(?i)(?:OTP|one[\s-]?time[\s-]?(?:password|code|PIN))""",
            """(?i)verification\s+code"""
        )

        return patterns.any { Regex(it).containsMatchIn(message) }
    }

    /**
     * Try to identify the bank from message content when sender is not verified.
     */
    private fun identifyBankFromContent(message: String): String {
        val bankKeywords = mapOf(
            "Commercial Bank of Ethiopia" to listOf("CBE", "Commercial Bank", "ComBank"),
            "Dashen Bank" to listOf("Dashen", "DashenBank"),
            "Awash Bank" to listOf("Awash", "AwashBank", "Awash International"),
            "Bank of Abyssinia" to listOf("Abyssinia", "BOA", "Bank of Abyssinia"),
            "Wegagen Bank" to listOf("Wegagen", "WegagenBank"),
            "Cooperative Bank of Oromia" to listOf("CoopBank", "Cooperative Bank", "CBO")
        )

        for ((bankName, keywords) in bankKeywords) {
            if (keywords.any { message.contains(it, ignoreCase = true) }) {
                return bankName
            }
        }
        return "Unknown Bank"
    }

    /**
     * Generic patterns used when no bank-specific format is found.
     */
    private fun getGenericPatterns(): Map<TransactionType, List<String>> {
        return mapOf(
            TransactionType.LOGIN_FAILED to listOf(
                """(?i)(?:failed|unsuccessful|invalid)\s*(?:login|sign[\s-]?in|attempt)"""
            ),
            TransactionType.PASSWORD_RESET to listOf(
                """(?i)(?:password|PIN)\s*(?:reset|change|update)"""
            ),
            TransactionType.DEVICE_CHANGE to listOf(
                """(?i)(?:new\s+)?(?:device|browser|phone)\s*(?:detected|added|logged|registered)"""
            ),
            TransactionType.OTP to listOf(
                """(?i)(?:OTP|one[\s-]?time[\s-]?(?:password|code|PIN))\s*(?:is|:)?\s*(\d{4,8})"""
            ),
            TransactionType.LOGIN to listOf(
                """(?i)(?:logged?\s*in|sign(?:ed)?\s*in|access(?:ed)?)\s*(?:to|from|on|your)"""
            ),
            TransactionType.CREDIT to listOf(
                """(?i)(?:credited|deposited|received|cr)\s*(?:with\s*)?(?:ETB|Birr)?""",
                """(?i)you\s+(?:have\s+)?received"""
            ),
            TransactionType.DEBIT to listOf(
                """(?i)(?:debited|withdrawn|sent|dr|paid|transferred)\s*(?:with\s*)?(?:ETB|Birr)?"""
            )
        )
    }

    /**
     * Batch parse multiple SMS messages.
     * Useful for initial scan of existing SMS when app is first installed.
     */
    fun parseBatch(messages: List<Pair<String, String>>, receivedAt: LocalDateTime = LocalDateTime.now()): List<ParsedSmsData> {
        return messages.map { (sender, body) -> parse(sender, body, receivedAt) }
    }
}

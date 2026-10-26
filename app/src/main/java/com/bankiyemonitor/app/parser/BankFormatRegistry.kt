package com.bankiyemonitor.app.parser

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Registry of bank-specific SMS formats and verified sender IDs.
 * This is the configurable rule engine that allows each bank's format
 * to be defined and updated without requiring an app update.
 */
class BankFormatRegistry(private val context: Context? = null) {

    private val bankFormats: MutableMap<String, BankFormat> = mutableMapOf()
    private val gson = Gson()

    init {
        loadDefaultFormats()
    }

    /**
     * Load default bank formats for Ethiopian banks in Wolaita Sodo
     */
    private fun loadDefaultFormats() {
        // Commercial Bank of Ethiopia (CBE)
        registerBank(BankFormat(
            bankId = "cbe",
            bankName = "Commercial Bank of Ethiopia",
            shortName = "CBE",
            verifiedSenderIds = setOf(
                "CBE", "ComBankET", "CBEBirr", "CBE-BANK",
                "921", "CBE-Alert", "CBEAlert"
            ),
            creditPatterns = listOf(
                """(?i)(?:credited|deposited|received|cr)\s*(?:with\s*)?(?:ETB|Birr)?\s*[\s:]*([\d,]+\.?\d*)""",
                """(?i)(?:ETB|Birr)\s*([\d,]+\.?\d*)\s*(?:has been\s+)?(?:credited|deposited|received)""",
                """(?i)(?:transferred|sent)\s+(?:to your|to acc).*?(?:ETB|Birr)?\s*([\d,]+\.?\d*)""",
                """(?i)you\s+(?:have\s+)?received\s+(?:ETB|Birr)?\s*([\d,]+\.?\d*)"""
            ),
            debitPatterns = listOf(
                """(?i)(?:debited|withdrawn|sent|dr|paid|transferred)\s*(?:with\s*)?(?:ETB|Birr)?\s*[\s:]*([\d,]+\.?\d*)""",
                """(?i)(?:ETB|Birr)\s*([\d,]+\.?\d*)\s*(?:has been\s+)?(?:debited|withdrawn|sent|deducted)""",
                """(?i)(?:purchase|payment|buy)\s+(?:of\s+)?(?:ETB|Birr)?\s*([\d,]+\.?\d*)"""
            ),
            balancePatterns = listOf(
                """(?i)(?:balance|bal|avl\.?\s*bal|available)\s*(?:is|:)?\s*(?:ETB|Birr)?\s*([\d,]+\.?\d*)""",
                """(?i)(?:ETB|Birr)\s*([\d,]+\.?\d*)\s*(?:is your|balance)"""
            ),
            loginPatterns = listOf(
                """(?i)(?:logged?\s*in|sign(?:ed)?\s*in|access(?:ed)?)\s*(?:to|from|on|your)""",
                """(?i)(?:new\s+)?(?:login|sign[\s-]?in|session)\s*(?:detected|attempt|from|on)""",
                """(?i)(?:your\s+)?account\s+(?:was\s+)?(?:accessed|logged\s*into)"""
            ),
            failedLoginPatterns = listOf(
                """(?i)(?:failed|unsuccessful|invalid)\s*(?:login|sign[\s-]?in|attempt)""",
                """(?i)(?:login|sign[\s-]?in)\s*(?:failed|unsuccessful|denied)""",
                """(?i)(?:wrong|incorrect)\s*(?:password|PIN|credential)"""
            ),
            passwordResetPatterns = listOf(
                """(?i)(?:password|PIN)\s*(?:reset|change|update)""",
                """(?i)(?:reset|change)\s*(?:your\s+)?(?:password|PIN)""",
                """(?i)(?:password|PIN)\s+(?:has been|was)\s+(?:changed|reset|updated)"""
            ),
            deviceChangePatterns = listOf(
                """(?i)(?:new\s+)?(?:device|browser|phone)\s*(?:detected|added|logged|registered)""",
                """(?i)(?:unrecognized|unknown)\s*(?:device|browser|phone)""",
                """(?i)(?:device|session)\s*(?:ID|token)\s*[:=]?\s*(\S+)"""
            ),
            otpPatterns = listOf(
                """(?i)(?:OTP|one[\s-]?time[\s-]?(?:password|code|PIN))\s*(?:is|:)?\s*(\d{4,8})""",
                """(?i)(\d{4,8})\s*(?:is\s+)?(?:your\s+)?(?:OTP|verification\s+code)""",
                """(?i)(?:code|PIN)\s*[:=]?\s*(\d{4,8})"""
            ),
            accountPatterns = listOf(
                """(?i)(?:a/?c|account|acct)\s*(?:no\.?|number|#)?\s*[:=]?\s*[*xX]*(\d{4,})""",
                """(?i)(?:account|a/?c)\s*(?:ending\s+(?:in|with))\s*(\d{4})"""
            ),
            referencePatterns = listOf(
                """(?i)(?:ref|reference|txn|transaction)\s*(?:no\.?|number|#|ID)?\s*[:=]?\s*([A-Za-z0-9]+)""",
                """(?i)(?:receipt|trace)\s*(?:no\.?|number|#)?\s*[:=]?\s*([A-Za-z0-9]+)"""
            ),
            counterpartyPatterns = listOf(
                """(?i)(?:from|to|sent to|received from|paid to|transferred to)\s+([A-Za-z\s]+?)(?:\s+(?:on|at|ref|ETB|Birr|\d)|$)""",
                """(?i)(?:beneficiary|recipient|payee)\s*[:=]?\s*([A-Za-z\s]+)"""
            ),
            timestampPatterns = listOf(
                """(\d{1,2}[/\-]\d{1,2}[/\-]\d{2,4}\s+\d{1,2}:\d{2}(?::\d{2})?\s*(?:AM|PM)?)""",
                """(\d{1,2}[/\-]\d{1,2}[/\-]\d{2,4})""",
                """(\d{1,2}:\d{2}(?::\d{2})?\s*(?:AM|PM)?)"""
            ),
            amountThousandSeparator = ",",
            amountDecimalSeparator = "."
        ))

        // Dashen Bank
        registerBank(BankFormat(
            bankId = "dashen",
            bankName = "Dashen Bank",
            shortName = "Dashen",
            verifiedSenderIds = setOf(
                "DashenBank", "Dashen", "DashenBk", "DASHEN",
                "DashenAlert", "Dashen-Bank"
            ),
            creditPatterns = listOf(
                """(?i)(?:credited|deposited|received|cr)\s*(?:with\s*)?(?:ETB|Birr)?\s*[\s:]*([\d,]+\.?\d*)""",
                """(?i)(?:ETB|Birr)\s*([\d,]+\.?\d*)\s*(?:has been\s+)?(?:credited|deposited|received)""",
                """(?i)you\s+(?:have\s+)?received\s+(?:ETB|Birr)?\s*([\d,]+\.?\d*)"""
            ),
            debitPatterns = listOf(
                """(?i)(?:debited|withdrawn|sent|dr|paid|transferred)\s*(?:with\s*)?(?:ETB|Birr)?\s*[\s:]*([\d,]+\.?\d*)""",
                """(?i)(?:ETB|Birr)\s*([\d,]+\.?\d*)\s*(?:has been\s+)?(?:debited|withdrawn|sent|deducted)"""
            ),
            balancePatterns = listOf(
                """(?i)(?:balance|bal|avl\.?\s*bal|available)\s*(?:is|:)?\s*(?:ETB|Birr)?\s*([\d,]+\.?\d*)"""
            ),
            loginPatterns = listOf(
                """(?i)(?:logged?\s*in|sign(?:ed)?\s*in|access(?:ed)?)\s*(?:to|from|on|your)""",
                """(?i)(?:new\s+)?(?:login|sign[\s-]?in|session)\s*(?:detected|attempt|from|on)"""
            ),
            failedLoginPatterns = listOf(
                """(?i)(?:failed|unsuccessful|invalid)\s*(?:login|sign[\s-]?in|attempt)"""
            ),
            passwordResetPatterns = listOf(
                """(?i)(?:password|PIN)\s*(?:reset|change|update)"""
            ),
            deviceChangePatterns = listOf(
                """(?i)(?:new\s+)?(?:device|browser|phone)\s*(?:detected|added|logged|registered)"""
            ),
            otpPatterns = listOf(
                """(?i)(?:OTP|one[\s-]?time[\s-]?(?:password|code|PIN))\s*(?:is|:)?\s*(\d{4,8})""",
                """(?i)(\d{4,8})\s*(?:is\s+)?(?:your\s+)?(?:OTP|verification\s+code)"""
            ),
            accountPatterns = listOf(
                """(?i)(?:a/?c|account|acct)\s*(?:no\.?|number|#)?\s*[:=]?\s*[*xX]*(\d{4,})"""
            ),
            referencePatterns = listOf(
                """(?i)(?:ref|reference|txn|transaction)\s*(?:no\.?|number|#|ID)?\s*[:=]?\s*([A-Za-z0-9]+)"""
            ),
            counterpartyPatterns = listOf(
                """(?i)(?:from|to|sent to|received from)\s+([A-Za-z\s]+?)(?:\s+(?:on|at|ref|ETB|Birr|\d)|$)"""
            ),
            timestampPatterns = listOf(
                """(\d{1,2}[/\-]\d{1,2}[/\-]\d{2,4}\s+\d{1,2}:\d{2}(?::\d{2})?\s*(?:AM|PM)?)""",
                """(\d{1,2}[/\-]\d{1,2}[/\-]\d{2,4})"""
            ),
            amountThousandSeparator = ",",
            amountDecimalSeparator = "."
        ))

        // Awash Bank
        registerBank(BankFormat(
            bankId = "awash",
            bankName = "Awash Bank",
            shortName = "Awash",
            verifiedSenderIds = setOf(
                "AwashBank", "Awash", "AwashBk", "AWASH",
                "AwashAlert", "Awash-Bank", "AwashIntBank"
            ),
            creditPatterns = listOf(
                """(?i)(?:credited|deposited|received|cr)\s*(?:with\s*)?(?:ETB|Birr)?\s*[\s:]*([\d,]+\.?\d*)""",
                """(?i)(?:ETB|Birr)\s*([\d,]+\.?\d*)\s*(?:has been\s+)?(?:credited|deposited|received)"""
            ),
            debitPatterns = listOf(
                """(?i)(?:debited|withdrawn|sent|dr|paid|transferred)\s*(?:with\s*)?(?:ETB|Birr)?\s*[\s:]*([\d,]+\.?\d*)""",
                """(?i)(?:ETB|Birr)\s*([\d,]+\.?\d*)\s*(?:has been\s+)?(?:debited|withdrawn|sent|deducted)"""
            ),
            balancePatterns = listOf(
                """(?i)(?:balance|bal|avl\.?\s*bal|available)\s*(?:is|:)?\s*(?:ETB|Birr)?\s*([\d,]+\.?\d*)"""
            ),
            loginPatterns = listOf(
                """(?i)(?:logged?\s*in|sign(?:ed)?\s*in|access(?:ed)?)\s*(?:to|from|on|your)"""
            ),
            failedLoginPatterns = listOf(
                """(?i)(?:failed|unsuccessful|invalid)\s*(?:login|sign[\s-]?in|attempt)"""
            ),
            passwordResetPatterns = listOf(
                """(?i)(?:password|PIN)\s*(?:reset|change|update)"""
            ),
            deviceChangePatterns = listOf(
                """(?i)(?:new\s+)?(?:device|browser|phone)\s*(?:detected|added|logged|registered)"""
            ),
            otpPatterns = listOf(
                """(?i)(?:OTP|one[\s-]?time[\s-]?(?:password|code|PIN))\s*(?:is|:)?\s*(\d{4,8})"""
            ),
            accountPatterns = listOf(
                """(?i)(?:a/?c|account|acct)\s*(?:no\.?|number|#)?\s*[:=]?\s*[*xX]*(\d{4,})"""
            ),
            referencePatterns = listOf(
                """(?i)(?:ref|reference|txn|transaction)\s*(?:no\.?|number|#|ID)?\s*[:=]?\s*([A-Za-z0-9]+)"""
            ),
            counterpartyPatterns = listOf(
                """(?i)(?:from|to|sent to|received from)\s+([A-Za-z\s]+?)(?:\s+(?:on|at|ref|ETB|Birr|\d)|$)"""
            ),
            timestampPatterns = listOf(
                """(\d{1,2}[/\-]\d{1,2}[/\-]\d{2,4}\s+\d{1,2}:\d{2}(?::\d{2})?\s*(?:AM|PM)?)"""
            ),
            amountThousandSeparator = ",",
            amountDecimalSeparator = "."
        ))

        // Bank of Abyssinia
        registerBank(BankFormat(
            bankId = "abyssinia",
            bankName = "Bank of Abyssinia",
            shortName = "BOA",
            verifiedSenderIds = setOf(
                "BOA", "Abyssinia", "BankAbyssinia", "BOABank",
                "BOA-Alert", "AbyssiniaBank", "BoABank"
            ),
            creditPatterns = listOf(
                """(?i)(?:credited|deposited|received|cr)\s*(?:with\s*)?(?:ETB|Birr)?\s*[\s:]*([\d,]+\.?\d*)""",
                """(?i)(?:ETB|Birr)\s*([\d,]+\.?\d*)\s*(?:has been\s+)?(?:credited|deposited|received)"""
            ),
            debitPatterns = listOf(
                """(?i)(?:debited|withdrawn|sent|dr|paid|transferred)\s*(?:with\s*)?(?:ETB|Birr)?\s*[\s:]*([\d,]+\.?\d*)""",
                """(?i)(?:ETB|Birr)\s*([\d,]+\.?\d*)\s*(?:has been\s+)?(?:debited|withdrawn|sent|deducted)"""
            ),
            balancePatterns = listOf(
                """(?i)(?:balance|bal|avl\.?\s*bal|available)\s*(?:is|:)?\s*(?:ETB|Birr)?\s*([\d,]+\.?\d*)"""
            ),
            loginPatterns = listOf(
                """(?i)(?:logged?\s*in|sign(?:ed)?\s*in|access(?:ed)?)\s*(?:to|from|on|your)"""
            ),
            failedLoginPatterns = listOf(
                """(?i)(?:failed|unsuccessful|invalid)\s*(?:login|sign[\s-]?in|attempt)"""
            ),
            passwordResetPatterns = listOf(
                """(?i)(?:password|PIN)\s*(?:reset|change|update)"""
            ),
            deviceChangePatterns = listOf(
                """(?i)(?:new\s+)?(?:device|browser|phone)\s*(?:detected|added|logged|registered)"""
            ),
            otpPatterns = listOf(
                """(?i)(?:OTP|one[\s-]?time[\s-]?(?:password|code|PIN))\s*(?:is|:)?\s*(\d{4,8})"""
            ),
            accountPatterns = listOf(
                """(?i)(?:a/?c|account|acct)\s*(?:no\.?|number|#)?\s*[:=]?\s*[*xX]*(\d{4,})"""
            ),
            referencePatterns = listOf(
                """(?i)(?:ref|reference|txn|transaction)\s*(?:no\.?|number|#|ID)?\s*[:=]?\s*([A-Za-z0-9]+)"""
            ),
            counterpartyPatterns = listOf(
                """(?i)(?:from|to|sent to|received from)\s+([A-Za-z\s]+?)(?:\s+(?:on|at|ref|ETB|Birr|\d)|$)"""
            ),
            timestampPatterns = listOf(
                """(\d{1,2}[/\-]\d{1,2}[/\-]\d{2,4}\s+\d{1,2}:\d{2}(?::\d{2})?\s*(?:AM|PM)?)"""
            ),
            amountThousandSeparator = ",",
            amountDecimalSeparator = "."
        ))

        // Wegagen Bank
        registerBank(BankFormat(
            bankId = "wegagen",
            bankName = "Wegagen Bank",
            shortName = "Wegagen",
            verifiedSenderIds = setOf(
                "Wegagen", "WegagenBank", "WegagenBk", "WEGAGEN",
                "Wegagen-Bank", "WegagenAlert"
            ),
            creditPatterns = listOf(
                """(?i)(?:credited|deposited|received|cr)\s*(?:with\s*)?(?:ETB|Birr)?\s*[\s:]*([\d,]+\.?\d*)"""
            ),
            debitPatterns = listOf(
                """(?i)(?:debited|withdrawn|sent|dr|paid|transferred)\s*(?:with\s*)?(?:ETB|Birr)?\s*[\s:]*([\d,]+\.?\d*)"""
            ),
            balancePatterns = listOf(
                """(?i)(?:balance|bal|avl\.?\s*bal|available)\s*(?:is|:)?\s*(?:ETB|Birr)?\s*([\d,]+\.?\d*)"""
            ),
            loginPatterns = listOf(
                """(?i)(?:logged?\s*in|sign(?:ed)?\s*in|access(?:ed)?)\s*(?:to|from|on|your)"""
            ),
            failedLoginPatterns = listOf(
                """(?i)(?:failed|unsuccessful|invalid)\s*(?:login|sign[\s-]?in|attempt)"""
            ),
            passwordResetPatterns = listOf(
                """(?i)(?:password|PIN)\s*(?:reset|change|update)"""
            ),
            deviceChangePatterns = listOf(
                """(?i)(?:new\s+)?(?:device|browser|phone)\s*(?:detected|added|logged|registered)"""
            ),
            otpPatterns = listOf(
                """(?i)(?:OTP|one[\s-]?time[\s-]?(?:password|code|PIN))\s*(?:is|:)?\s*(\d{4,8})"""
            ),
            accountPatterns = listOf(
                """(?i)(?:a/?c|account|acct)\s*(?:no\.?|number|#)?\s*[:=]?\s*[*xX]*(\d{4,})"""
            ),
            referencePatterns = listOf(
                """(?i)(?:ref|reference|txn|transaction)\s*(?:no\.?|number|#|ID)?\s*[:=]?\s*([A-Za-z0-9]+)"""
            ),
            counterpartyPatterns = listOf(
                """(?i)(?:from|to|sent to|received from)\s+([A-Za-z\s]+?)(?:\s+(?:on|at|ref|ETB|Birr|\d)|$)"""
            ),
            timestampPatterns = listOf(
                """(\d{1,2}[/\-]\d{1,2}[/\-]\d{2,4}\s+\d{1,2}:\d{2}(?::\d{2})?\s*(?:AM|PM)?)"""
            ),
            amountThousandSeparator = ",",
            amountDecimalSeparator = "."
        ))

        // Cooperative Bank of Oromia (Coopbank)
        registerBank(BankFormat(
            bankId = "coopbank",
            bankName = "Cooperative Bank of Oromia",
            shortName = "CoopBank",
            verifiedSenderIds = setOf(
                "CoopBank", "Coopbank", "CBO", "CoopBankET",
                "CoopAlert", "Coop-Bank"
            ),
            creditPatterns = listOf(
                """(?i)(?:credited|deposited|received|cr)\s*(?:with\s*)?(?:ETB|Birr)?\s*[\s:]*([\d,]+\.?\d*)"""
            ),
            debitPatterns = listOf(
                """(?i)(?:debited|withdrawn|sent|dr|paid|transferred)\s*(?:with\s*)?(?:ETB|Birr)?\s*[\s:]*([\d,]+\.?\d*)"""
            ),
            balancePatterns = listOf(
                """(?i)(?:balance|bal|avl\.?\s*bal|available)\s*(?:is|:)?\s*(?:ETB|Birr)?\s*([\d,]+\.?\d*)"""
            ),
            loginPatterns = listOf(
                """(?i)(?:logged?\s*in|sign(?:ed)?\s*in|access(?:ed)?)\s*(?:to|from|on|your)"""
            ),
            failedLoginPatterns = listOf(
                """(?i)(?:failed|unsuccessful|invalid)\s*(?:login|sign[\s-]?in|attempt)"""
            ),
            passwordResetPatterns = listOf(
                """(?i)(?:password|PIN)\s*(?:reset|change|update)"""
            ),
            deviceChangePatterns = listOf(
                """(?i)(?:new\s+)?(?:device|browser|phone)\s*(?:detected|added|logged|registered)"""
            ),
            otpPatterns = listOf(
                """(?i)(?:OTP|one[\s-]?time[\s-]?(?:password|code|PIN))\s*(?:is|:)?\s*(\d{4,8})"""
            ),
            accountPatterns = listOf(
                """(?i)(?:a/?c|account|acct)\s*(?:no\.?|number|#)?\s*[:=]?\s*[*xX]*(\d{4,})"""
            ),
            referencePatterns = listOf(
                """(?i)(?:ref|reference|txn|transaction)\s*(?:no\.?|number|#|ID)?\s*[:=]?\s*([A-Za-z0-9]+)"""
            ),
            counterpartyPatterns = listOf(
                """(?i)(?:from|to|sent to|received from)\s+([A-Za-z\s]+?)(?:\s+(?:on|at|ref|ETB|Birr|\d)|$)"""
            ),
            timestampPatterns = listOf(
                """(\d{1,2}[/\-]\d{1,2}[/\-]\d{2,4}\s+\d{1,2}:\d{2}(?::\d{2})?\s*(?:AM|PM)?)"""
            ),
            amountThousandSeparator = ",",
            amountDecimalSeparator = "."
        ))
    }

    fun registerBank(format: BankFormat) {
        bankFormats[format.bankId] = format
    }

    fun getAllFormats(): Map<String, BankFormat> = bankFormats.toMap()

    fun getFormatById(bankId: String): BankFormat? = bankFormats[bankId]

    /**
     * Find the bank format that matches a given sender address.
     * Returns null if no registered bank matches.
     */
    fun findBankBySender(senderAddress: String): BankFormat? {
        val normalizedSender = senderAddress.trim().uppercase()
        return bankFormats.values.find { format ->
            format.verifiedSenderIds.any { it.uppercase() == normalizedSender }
        }
    }

    /**
     * Check if a sender is in ANY bank's whitelist
     */
    fun isVerifiedSender(senderAddress: String): Boolean {
        return findBankBySender(senderAddress) != null
    }

    /**
     * Get all verified sender IDs across all banks
     */
    fun getAllVerifiedSenders(): Set<String> {
        return bankFormats.values.flatMap { it.verifiedSenderIds }.toSet()
    }
}

/**
 * Defines the SMS format rules for a specific bank.
 * Each bank has its own set of regex patterns for extracting data.
 */
data class BankFormat(
    val bankId: String,
    val bankName: String,
    val shortName: String,
    val verifiedSenderIds: Set<String>,
    val creditPatterns: List<String>,
    val debitPatterns: List<String>,
    val balancePatterns: List<String>,
    val loginPatterns: List<String>,
    val failedLoginPatterns: List<String>,
    val passwordResetPatterns: List<String>,
    val deviceChangePatterns: List<String>,
    val otpPatterns: List<String>,
    val accountPatterns: List<String>,
    val referencePatterns: List<String>,
    val counterpartyPatterns: List<String>,
    val timestampPatterns: List<String>,
    val amountThousandSeparator: String = ",",
    val amountDecimalSeparator: String = "."
)

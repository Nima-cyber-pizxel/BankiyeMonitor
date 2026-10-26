package com.bankiyemonitor.app.parser

import com.bankiyemonitor.app.parser.model.SmsToken
import com.bankiyemonitor.app.parser.model.TokenType

/**
 * Lexical analyzer (tokenizer) for bank SMS messages.
 * Breaks raw SMS text into classified tokens for structured extraction.
 */
class SmsTokenizer {

    private val tokenPatterns = listOf(
        // Order matters - more specific patterns first
        TokenPattern(TokenType.URL, """https?://\S+"""),
        TokenPattern(TokenType.OTP_CODE, """(?<!\d)\d{4,8}(?!\d)(?=\s|$|[.,)]|\s*is\s+your\s+OTP)"""),
        TokenPattern(TokenType.AMOUNT, """(?:ETB|Birr)\s*[\d,]+\.?\d*|[\d,]+\.?\d*\s*(?:ETB|Birr)"""),
        TokenPattern(TokenType.CURRENCY, """(?i)\b(?:ETB|Birr)\b"""),
        TokenPattern(TokenType.ACCOUNT_NUMBER, """(?i)(?:a/?c|account|acct)\s*(?:no\.?|#)?\s*[:=]?\s*[*xX]*\d{4,}"""),
        TokenPattern(TokenType.REFERENCE, """(?i)(?:ref|txn|transaction)\s*(?:no\.?|#|ID)?\s*[:=]?\s*[A-Za-z0-9]{6,}"""),
        TokenPattern(TokenType.DATE, """\d{1,2}[/\-]\d{1,2}[/\-]\d{2,4}"""),
        TokenPattern(TokenType.TIME, """\d{1,2}:\d{2}(?::\d{2})?\s*(?:AM|PM)?"""),
        TokenPattern(TokenType.PHONE_NUMBER, """(?:\+251|0)\d{9}"""),
        TokenPattern(TokenType.DEVICE_ID, """(?i)(?:device|session)\s*(?:ID|token)\s*[:=]?\s*\S+""")
    )

    /**
     * Tokenize a raw SMS message into classified tokens.
     */
    fun tokenize(message: String): List<SmsToken> {
        val tokens = mutableListOf<SmsToken>()
        val usedRanges = mutableListOf<IntRange>()

        for (pattern in tokenPatterns) {
            val regex = Regex(pattern.regex)
            val matches = regex.findAll(message)
            
            for (match in matches) {
                val range = match.range
                // Skip if this range overlaps with an already-matched token
                if (usedRanges.none { it.overlaps(range) }) {
                    tokens.add(SmsToken(
                        type = pattern.tokenType,
                        value = match.value.trim(),
                        position = range
                    ))
                    usedRanges.add(range)
                }
            }
        }

        // Sort by position
        return tokens.sortedBy { it.position.first }
    }

    private fun IntRange.overlaps(other: IntRange): Boolean {
        return this.first <= other.last && other.first <= this.last
    }

    private data class TokenPattern(
        val tokenType: TokenType,
        val regex: String
    )
}

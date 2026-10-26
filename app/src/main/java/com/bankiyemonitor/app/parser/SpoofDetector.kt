package com.bankiyemonitor.app.parser

import com.bankiyemonitor.app.parser.model.RiskLevel
import com.bankiyemonitor.app.parser.model.VerificationStatus

/**
 * Spoof Detection & Sender Verification Engine.
 * 
 * Rather than trusting the display name (which scammers can easily fake),
 * the detector maintains a whitelist of verified sender IDs for each bank.
 * Every incoming SMS is checked against this whitelist before it is processed.
 */
class SpoofDetector(private val bankFormatRegistry: BankFormatRegistry) {

    // Known spoof patterns - words scammers commonly use to mimic bank names
    private val spoofIndicators = listOf(
        """(?i)^(CBE|Dashen|Awash|Abyssinia|BOA|Wegagen|Coop)\d+$""",  // Bank name + numbers
        """(?i)(bank|alert|verify|secure|update|urgent)""",              // Generic bank keywords as sender
        """(?i)^\+\d{7,15}$""",                                          // Phone numbers pretending to be banks
        """(?i)^0\d{9}$"""                                                // Local phone numbers
    )

    // Urgency/phishing keywords that indicate social engineering
    private val urgencyKeywords = listOf(
        "urgent", "immediately", "expire", "expired", "suspend",
        "suspended", "block", "blocked", "verify now", "click here",
        "click link", "update now", "confirm now", "act now",
        "limited time", "within 24 hours", "account will be",
        "failure to", "unauthorized", "fraudulent", "security breach",
        "verify your identity", "confirm your details", "update your information"
    )

    // Link patterns that may indicate phishing
    private val phishingLinkPatterns = listOf(
        """https?://\S+""",
        """bit\.ly/\S+""",
        """tinyurl\.com/\S+""",
        """goo\.gl/\S+""",
        """t\.co/\S+""",
        """\b\w+\.(tk|ml|ga|cf|gq)/\S*""",  // Free domain TLDs commonly used for phishing
        """(?i)(?:click|tap|open|visit|go\s+to)\s+(?:this\s+)?(?:link|url|here)"""
    )

    data class SpoofAnalysis(
        val verificationStatus: VerificationStatus,
        val riskLevel: RiskLevel,
        val reasons: List<String>,
        val matchedBank: BankFormat?,
        val urgencyScore: Int,
        val containsLink: Boolean,
        val isSuspectedPhishing: Boolean
    )

    /**
     * Perform comprehensive spoof analysis on an SMS message.
     */
    fun analyze(senderAddress: String, messageBody: String): SpoofAnalysis {
        val reasons = mutableListOf<String>()
        var riskLevel = RiskLevel.LOW
        
        // Step 1: Check sender against whitelist
        val matchedBank = bankFormatRegistry.findBankBySender(senderAddress)
        val verificationStatus: VerificationStatus
        
        if (matchedBank != null) {
            verificationStatus = VerificationStatus.VERIFIED
        } else {
            // Check if sender is trying to look like a bank
            val looksLikeBank = spoofIndicators.any { pattern ->
                Regex(pattern).containsMatchIn(senderAddress)
            }
            
            val mentionsBanking = messageBody.contains(Regex(
                """(?i)(bank|account|balance|transfer|ETB|Birr|debit|credit|transaction)"""
            ))
            
            if (looksLikeBank && mentionsBanking) {
                verificationStatus = VerificationStatus.SPOOFED
                riskLevel = RiskLevel.CRITICAL
                reasons.add("Sender '$senderAddress' is not in any bank's verified sender list but claims to be a bank")
            } else if (mentionsBanking) {
                verificationStatus = VerificationStatus.UNVERIFIED
                riskLevel = RiskLevel.MEDIUM
                reasons.add("Message contains banking keywords but sender is not verified")
            } else {
                verificationStatus = VerificationStatus.UNVERIFIED
                reasons.add("Sender not in verified bank sender list")
            }
        }

        // Step 2: Check for urgency/social engineering language
        val urgencyScore = calculateUrgencyScore(messageBody)
        if (urgencyScore > 60) {
            riskLevel = maxOf(riskLevel, RiskLevel.HIGH)
            reasons.add("High urgency language detected (score: $urgencyScore/100)")
        } else if (urgencyScore > 30) {
            riskLevel = maxOf(riskLevel, RiskLevel.MEDIUM)
            reasons.add("Moderate urgency language detected (score: $urgencyScore/100)")
        }

        // Step 3: Check for suspicious links
        val containsLink = phishingLinkPatterns.any { pattern ->
            Regex(pattern).containsMatchIn(messageBody)
        }
        
        val isSuspectedPhishing: Boolean
        if (containsLink) {
            // Legitimate banks rarely send links in SMS
            riskLevel = maxOf(riskLevel, RiskLevel.HIGH)
            reasons.add("Message contains links - potential phishing attempt")
            
            // Check for URL shorteners (extra suspicious)
            val hasShortUrl = listOf("bit.ly", "tinyurl", "goo.gl", "t.co").any {
                messageBody.lowercase().contains(it)
            }
            if (hasShortUrl) {
                riskLevel = RiskLevel.CRITICAL
                reasons.add("Message contains shortened URLs - highly suspicious")
            }
            
            isSuspectedPhishing = verificationStatus != VerificationStatus.VERIFIED
        } else {
            isSuspectedPhishing = false
        }

        // Step 4: Check for OTP with urgency (common social engineering combo)
        val hasOtp = messageBody.contains(Regex("""(?i)(OTP|one[\s-]?time|verification\s+code)"""))
        if (hasOtp && urgencyScore > 40) {
            riskLevel = maxOf(riskLevel, RiskLevel.HIGH)
            reasons.add("OTP request combined with urgency language - possible social engineering")
        }

        return SpoofAnalysis(
            verificationStatus = verificationStatus,
            riskLevel = riskLevel,
            reasons = reasons,
            matchedBank = matchedBank,
            urgencyScore = urgencyScore,
            containsLink = containsLink,
            isSuspectedPhishing = isSuspectedPhishing
        )
    }

    /**
     * Calculate urgency score (0-100) based on social engineering indicators.
     */
    private fun calculateUrgencyScore(message: String): Int {
        val lowerMessage = message.lowercase()
        var score = 0
        var matchedKeywords = 0

        urgencyKeywords.forEach { keyword ->
            if (lowerMessage.contains(keyword.lowercase())) {
                matchedKeywords++
                score += when {
                    keyword in listOf("urgent", "immediately", "act now", "click here", "click link") -> 20
                    keyword in listOf("suspend", "suspended", "block", "blocked") -> 15
                    keyword in listOf("expire", "expired", "verify now", "confirm now") -> 15
                    keyword in listOf("security breach", "unauthorized", "fraudulent") -> 20
                    else -> 10
                }
            }
        }

        // Cap at 100
        return minOf(score, 100)
    }

    /**
     * Quick check if a sender is verified
     */
    fun isVerifiedSender(senderAddress: String): Boolean {
        return bankFormatRegistry.isVerifiedSender(senderAddress)
    }
}

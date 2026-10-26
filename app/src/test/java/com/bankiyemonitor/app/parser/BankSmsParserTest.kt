package com.bankiyemonitor.app.parser

import com.bankiyemonitor.app.parser.model.RiskLevel
import com.bankiyemonitor.app.parser.model.TransactionType
import com.bankiyemonitor.app.parser.model.VerificationStatus
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * Unit tests verifying:
 * 1. Spoof Detection & Sender Verification
 * 2. Unstructured to Structured Data Transformation (CBE, Dashen, Awash, BOA, Wegagen, CoopBank)
 * 3. Security Event Recognition (Failed logins, Password resets, New devices/sessions)
 * 4. Social Engineering / Urgency / Phishing Link Scoring
 */
class BankSmsParserTest {

    private lateinit var registry: BankFormatRegistry
    private lateinit var spoofDetector: SpoofDetector
    private lateinit var parser: BankSmsParser

    @Before
    fun setUp() {
        registry = BankFormatRegistry()
        spoofDetector = SpoofDetector(registry)
        parser = BankSmsParser(registry, spoofDetector)
    }

    @Test
    fun testCommercialBankOfEthiopia_CreditAlert() {
        val sender = "CBE"
        val message = "Dear Customer, Your account 100012345678 has been credited with ETB 15,450.50 from ABEBE KEBEDE on 25/10/2026 14:30. Available balance is ETB 45,200.75. Ref: TXN987654321."

        val result = parser.parse(sender, message)

        assertEquals("Commercial Bank of Ethiopia", result.bankName)
        assertEquals(VerificationStatus.VERIFIED, result.verificationStatus)
        assertEquals(TransactionType.CREDIT, result.transactionType)
        assertEquals(15450.50, result.amount ?: 0.0, 0.01)
        assertEquals(45200.75, result.balance ?: 0.0, 0.01)
        assertEquals(RiskLevel.LOW, result.riskLevel)
        assertFalse(result.containsLink)
    }

    @Test
    fun testDashenBank_DebitAlert() {
        val sender = "DashenBank"
        val message = "Your account 0023456789 was debited with ETB 2,500.00 for payment to Sodo Supermarket on 25/10/2026. Avail Bal: ETB 12,300.00. Ref: DSH88776655."

        val result = parser.parse(sender, message)

        assertEquals("Dashen Bank", result.bankName)
        assertEquals(VerificationStatus.VERIFIED, result.verificationStatus)
        assertEquals(TransactionType.DEBIT, result.transactionType)
        assertEquals(2500.00, result.amount ?: 0.0, 0.01)
        assertEquals(12300.00, result.balance ?: 0.0, 0.01)
        assertEquals(RiskLevel.LOW, result.riskLevel)
    }

    @Test
    fun testSpoofDetection_FakeCbeSender() {
        // Scammer uses a random mobile number or spoofed alphanumeric not in registry
        val sender = "+251911223344"
        val message = "CBE Alert: Your account is suspended due to unauthorized activity. Verify immediately at http://cbe-login-update.tk to avoid account freeze."

        val result = parser.parse(sender, message)

        assertNotEquals(VerificationStatus.VERIFIED, result.verificationStatus)
        assertTrue(result.containsLink)
        assertTrue(result.urgencyScore > 50)
        assertEquals(RiskLevel.CRITICAL, result.riskLevel)
    }

    @Test
    fun testSecurityAlert_FailedLoginAttempt() {
        val sender = "BOA"
        val message = "Security Alert: 3 failed login attempts detected on your Bank of Abyssinia mobile app from unrecognized device ID: SM-A525F on 25/10/2026 15:10."

        val result = parser.parse(sender, message)

        assertEquals("Bank of Abyssinia", result.bankName)
        assertEquals(VerificationStatus.VERIFIED, result.verificationStatus)
        assertEquals(TransactionType.LOGIN_FAILED, result.transactionType)
        assertEquals(RiskLevel.LOW, result.riskLevel) // sender verified, but classified as security event
    }

    @Test
    fun testSecurityAlert_PasswordReset() {
        val sender = "AwashBank"
        val message = "Your Awash Online Banking password reset request was initiated. OTP code is 849201. Valid for 5 minutes. Do not share this code."

        val result = parser.parse(sender, message)

        assertEquals("Awash Bank", result.bankName)
        assertEquals(VerificationStatus.VERIFIED, result.verificationStatus)
        assertTrue(result.otpDetected)
    }

    @Test
    fun testSecurityAlert_NewDeviceDetection() {
        val sender = "CBE"
        val message = "CBE Alert: New device detected logging into CBE Birr from Chrome Browser on Windows. Session token: SES-99128."

        val result = parser.parse(sender, message)

        assertEquals("Commercial Bank of Ethiopia", result.bankName)
        assertEquals(TransactionType.DEVICE_CHANGE, result.transactionType)
        assertNotNull(result.deviceInfo)
    }

    @Test
    fun testPhishing_ShortUrlWithUrgency() {
        val sender = "CBE-Bank" // looks like a bank, but check spoof analysis
        val message = "Urgent: Your CBE account will be blocked within 24 hours. Click link bit.ly/cbe-verify-now to confirm now."

        val spoofAnalysis = spoofDetector.analyze(sender, message)

        assertTrue(spoofAnalysis.containsLink)
        assertTrue(spoofAnalysis.urgencyScore >= 40)
        assertEquals(RiskLevel.CRITICAL, spoofAnalysis.riskLevel)
    }
}

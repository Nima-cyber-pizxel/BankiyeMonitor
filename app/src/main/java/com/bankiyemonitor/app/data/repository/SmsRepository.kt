package com.bankiyemonitor.app.data.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import com.bankiyemonitor.app.data.local.dao.ParsedSmsDao
import com.bankiyemonitor.app.data.local.entity.ParsedSmsEntity
import com.bankiyemonitor.app.parser.BankSmsParser
import com.bankiyemonitor.app.parser.model.ParsedSmsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import com.google.gson.Gson

class SmsRepository(
    private val context: Context,
    private val parsedSmsDao: ParsedSmsDao,
    private val bankSmsParser: BankSmsParser
) {
    private val gson = Gson()

    /**
     * Read all SMS messages from the device and parse bank-related ones.
     * This is the initial scan that runs when the app is first set up.
     */
    suspend fun scanExistingSms(): List<ParsedSmsData> = withContext(Dispatchers.IO) {
        val parsedMessages = mutableListOf<ParsedSmsData>()
        
        val cursor: Cursor? = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE
            ),
            "${Telephony.Sms.TYPE} = ?",
            arrayOf(Telephony.Sms.MESSAGE_TYPE_INBOX.toString()),
            "${Telephony.Sms.DATE} DESC"
        )

        cursor?.use {
            val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
            val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)

            while (it.moveToNext()) {
                val address = it.getString(addressIndex) ?: continue
                val body = it.getString(bodyIndex) ?: continue
                val date = it.getLong(dateIndex)

                val receivedAt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(date),
                    ZoneId.systemDefault()
                )

                try {
                    val parsed = bankSmsParser.parse(address, body, receivedAt)
                    // Only keep bank-related messages
                    if (parsed.bankName != "Unknown Bank" || isBankRelated(body)) {
                        parsedMessages.add(parsed)
                    }
                } catch (e: Exception) {
                    // Skip unparseable messages
                }
            }
        }

        // Save to database
        val entities = parsedMessages.map { it.toEntity() }
        parsedSmsDao.insertAll(entities)

        parsedMessages
    }

    /**
     * Parse and save a single new SMS message.
     */
    suspend fun parseAndSave(senderAddress: String, messageBody: String): ParsedSmsEntity? = withContext(Dispatchers.IO) {
        // Check for duplicates
        val existing = parsedSmsDao.findDuplicate(messageBody, senderAddress)
        if (existing != null) return@withContext null

        val parsed = bankSmsParser.parse(senderAddress, messageBody)
        val entity = parsed.toEntity()
        val id = parsedSmsDao.insert(entity)
        entity.copy(id = id)
    }

    fun getAllParsedSms(): Flow<List<ParsedSmsEntity>> = parsedSmsDao.getAllSms()

    fun getHighRiskSms(): Flow<List<ParsedSmsEntity>> = parsedSmsDao.getHighRiskSms()

    fun getSpoofedSms(): Flow<List<ParsedSmsEntity>> = parsedSmsDao.getSpoofedSms()

    fun getSmsByType(type: String): Flow<List<ParsedSmsEntity>> = parsedSmsDao.getSmsByType(type)

    fun getSmsByDateRange(startTime: Long, endTime: Long): Flow<List<ParsedSmsEntity>> =
        parsedSmsDao.getSmsByDateRange(startTime, endTime)

    suspend fun markAsRead(id: Long) = parsedSmsDao.markAsRead(id)

    suspend fun setFlagged(id: Long, flagged: Boolean) = parsedSmsDao.setFlagged(id, flagged)

    /**
     * Quick check if SMS text contains banking keywords.
     */
    private fun isBankRelated(message: String): Boolean {
        val bankKeywords = listOf(
            "ETB", "Birr", "credited", "debited", "transfer",
            "balance", "account", "bank", "OTP", "login",
            "password", "PIN", "transaction", "withdrawal"
        )
        val lowerMessage = message.lowercase()
        return bankKeywords.any { lowerMessage.contains(it.lowercase()) }
    }

    /**
     * Convert ParsedSmsData to database entity.
     */
    private fun ParsedSmsData.toEntity(): ParsedSmsEntity {
        return ParsedSmsEntity(
            rawMessage = rawMessage,
            senderAddress = senderAddress,
            transactionType = transactionType.name,
            amount = amount,
            currency = currency,
            counterparty = counterparty,
            accountNumber = accountNumber,
            referenceNumber = referenceNumber,
            balance = balance,
            timestamp = timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            parsedAt = parsedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            bankName = bankName,
            verificationStatus = verificationStatus.name,
            riskLevel = riskLevel.name,
            riskReasons = gson.toJson(riskReasons),
            deviceInfo = deviceInfo,
            sessionInfo = sessionInfo,
            otpDetected = otpDetected,
            urgencyScore = urgencyScore,
            containsLink = containsLink
        )
    }
}

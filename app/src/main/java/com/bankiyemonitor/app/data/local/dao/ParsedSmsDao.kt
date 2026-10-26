package com.bankiyemonitor.app.data.local.dao

import androidx.room.*
import com.bankiyemonitor.app.data.local.entity.ParsedSmsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParsedSmsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sms: ParsedSmsEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(smsList: List<ParsedSmsEntity>): List<Long>

    @Update
    suspend fun update(sms: ParsedSmsEntity)

    @Delete
    suspend fun delete(sms: ParsedSmsEntity)

    @Query("SELECT * FROM parsed_sms ORDER BY timestamp DESC")
    fun getAllSms(): Flow<List<ParsedSmsEntity>>

    @Query("SELECT * FROM parsed_sms WHERE id = :id")
    suspend fun getSmsById(id: Long): ParsedSmsEntity?

    @Query("SELECT * FROM parsed_sms WHERE transactionType = :type ORDER BY timestamp DESC")
    fun getSmsByType(type: String): Flow<List<ParsedSmsEntity>>

    @Query("SELECT * FROM parsed_sms WHERE bankName = :bankName ORDER BY timestamp DESC")
    fun getSmsByBank(bankName: String): Flow<List<ParsedSmsEntity>>

    @Query("SELECT * FROM parsed_sms WHERE riskLevel IN ('HIGH', 'CRITICAL') ORDER BY timestamp DESC")
    fun getHighRiskSms(): Flow<List<ParsedSmsEntity>>

    @Query("SELECT * FROM parsed_sms WHERE verificationStatus = 'SPOOFED' ORDER BY timestamp DESC")
    fun getSpoofedSms(): Flow<List<ParsedSmsEntity>>

    @Query("SELECT * FROM parsed_sms WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getSmsByDateRange(startTime: Long, endTime: Long): Flow<List<ParsedSmsEntity>>

    @Query("SELECT * FROM parsed_sms WHERE transactionType IN ('CREDIT', 'DEBIT') AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<ParsedSmsEntity>>

    @Query("SELECT SUM(amount) FROM parsed_sms WHERE transactionType = 'CREDIT' AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getTotalCredited(startTime: Long, endTime: Long): Double?

    @Query("SELECT SUM(amount) FROM parsed_sms WHERE transactionType = 'DEBIT' AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getTotalDebited(startTime: Long, endTime: Long): Double?

    @Query("SELECT COUNT(*) FROM parsed_sms WHERE transactionType = 'LOGIN' AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getLoginCount(startTime: Long, endTime: Long): Int

    @Query("SELECT COUNT(*) FROM parsed_sms WHERE transactionType = 'LOGIN_FAILED' AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getFailedLoginCount(startTime: Long, endTime: Long): Int

    @Query("SELECT COUNT(*) FROM parsed_sms WHERE transactionType = 'PASSWORD_RESET' AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getPasswordResetCount(startTime: Long, endTime: Long): Int

    @Query("SELECT COUNT(*) FROM parsed_sms WHERE transactionType = 'DEVICE_CHANGE' AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getDeviceChangeCount(startTime: Long, endTime: Long): Int

    @Query("SELECT COUNT(*) FROM parsed_sms WHERE verificationStatus = 'SPOOFED' AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getSpoofedCount(startTime: Long, endTime: Long): Int

    @Query("SELECT COUNT(*) FROM parsed_sms WHERE riskLevel IN ('HIGH', 'CRITICAL') AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getHighRiskCount(startTime: Long, endTime: Long): Int

    @Query("SELECT DISTINCT bankName FROM parsed_sms")
    suspend fun getAllBankNames(): List<String>

    @Query("SELECT COUNT(*) FROM parsed_sms")
    suspend fun getTotalCount(): Int

    @Query("SELECT * FROM parsed_sms WHERE isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadSms(): Flow<List<ParsedSmsEntity>>

    @Query("UPDATE parsed_sms SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE parsed_sms SET isFlagged = :flagged WHERE id = :id")
    suspend fun setFlagged(id: Long, flagged: Boolean)

    @Query("SELECT * FROM parsed_sms WHERE rawMessage = :rawMessage AND senderAddress = :sender LIMIT 1")
    suspend fun findDuplicate(rawMessage: String, sender: String): ParsedSmsEntity?
}

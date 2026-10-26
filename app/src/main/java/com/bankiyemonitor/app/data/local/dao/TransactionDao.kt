package com.bankiyemonitor.app.data.local.dao

import androidx.room.*
import com.bankiyemonitor.app.data.local.entity.TransactionSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(summary: TransactionSummaryEntity): Long

    @Update
    suspend fun update(summary: TransactionSummaryEntity)

    @Delete
    suspend fun delete(summary: TransactionSummaryEntity)

    @Query("SELECT * FROM transaction_summaries ORDER BY periodStart DESC")
    fun getAllSummaries(): Flow<List<TransactionSummaryEntity>>

    @Query("SELECT * FROM transaction_summaries WHERE periodType = :periodType ORDER BY periodStart DESC")
    fun getSummariesByType(periodType: String): Flow<List<TransactionSummaryEntity>>

    @Query("SELECT * FROM transaction_summaries WHERE periodType = :periodType ORDER BY periodStart DESC LIMIT 1")
    suspend fun getLatestSummary(periodType: String): TransactionSummaryEntity?

    @Query("SELECT * FROM transaction_summaries WHERE periodType = :periodType AND periodStart BETWEEN :startTime AND :endTime")
    suspend fun getSummaryForPeriod(periodType: String, startTime: Long, endTime: Long): TransactionSummaryEntity?

    @Query("SELECT * FROM transaction_summaries WHERE periodType = :periodType ORDER BY periodStart DESC LIMIT :count")
    fun getRecentSummaries(periodType: String, count: Int): Flow<List<TransactionSummaryEntity>>
}

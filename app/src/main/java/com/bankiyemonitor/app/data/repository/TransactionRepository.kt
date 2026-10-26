package com.bankiyemonitor.app.data.repository

import com.bankiyemonitor.app.data.local.dao.TransactionDao
import com.bankiyemonitor.app.data.local.entity.TransactionSummaryEntity
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao
) {
    fun getAllSummaries(): Flow<List<TransactionSummaryEntity>> = transactionDao.getAllSummaries()

    fun getSummariesByType(periodType: String): Flow<List<TransactionSummaryEntity>> =
        transactionDao.getSummariesByType(periodType)

    suspend fun getLatestSummary(periodType: String): TransactionSummaryEntity? =
        transactionDao.getLatestSummary(periodType)

    suspend fun saveSummary(summary: TransactionSummaryEntity): Long =
        transactionDao.insert(summary)

    fun getRecentSummaries(periodType: String, count: Int): Flow<List<TransactionSummaryEntity>> =
        transactionDao.getRecentSummaries(periodType, count)
}

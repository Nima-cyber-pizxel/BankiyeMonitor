package com.bankiyemonitor.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bankiyemonitor.app.data.local.dao.ParsedSmsDao
import com.bankiyemonitor.app.data.local.dao.SecurityEventDao
import com.bankiyemonitor.app.data.local.dao.TransactionDao
import com.bankiyemonitor.app.data.local.entity.ParsedSmsEntity
import com.bankiyemonitor.app.data.local.entity.SecurityEventEntity
import com.bankiyemonitor.app.data.local.entity.TransactionSummaryEntity

@Database(
    entities = [
        ParsedSmsEntity::class,
        SecurityEventEntity::class,
        TransactionSummaryEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class BankiyeDatabase : RoomDatabase() {
    abstract fun parsedSmsDao(): ParsedSmsDao
    abstract fun securityEventDao(): SecurityEventDao
    abstract fun transactionDao(): TransactionDao
}

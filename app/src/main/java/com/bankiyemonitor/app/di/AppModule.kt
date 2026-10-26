package com.bankiyemonitor.app.di

import android.content.Context
import androidx.room.Room
import com.bankiyemonitor.app.data.local.BankiyeDatabase
import com.bankiyemonitor.app.data.local.dao.ParsedSmsDao
import com.bankiyemonitor.app.data.local.dao.SecurityEventDao
import com.bankiyemonitor.app.data.local.dao.TransactionDao
import com.bankiyemonitor.app.data.preferences.UserPreferencesManager
import com.bankiyemonitor.app.data.repository.SmsRepository
import com.bankiyemonitor.app.data.repository.TransactionRepository
import com.bankiyemonitor.app.data.repository.SecurityRepository
import com.bankiyemonitor.app.parser.BankSmsParser
import com.bankiyemonitor.app.parser.SpoofDetector
import com.bankiyemonitor.app.parser.BankFormatRegistry
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        preferencesManager: UserPreferencesManager
    ): BankiyeDatabase {
        val passphrase = preferencesManager.getDatabasePassphrase()
        val factory = SupportFactory(passphrase)
        
        return Room.databaseBuilder(
            context,
            BankiyeDatabase::class.java,
            "bankiye_monitor.db"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideParsedSmsDao(database: BankiyeDatabase): ParsedSmsDao = database.parsedSmsDao()

    @Provides
    @Singleton
    fun provideTransactionDao(database: BankiyeDatabase): TransactionDao = database.transactionDao()

    @Provides
    @Singleton
    fun provideSecurityEventDao(database: BankiyeDatabase): SecurityEventDao = database.securityEventDao()

    @Provides
    @Singleton
    fun provideBankFormatRegistry(@ApplicationContext context: Context): BankFormatRegistry {
        return BankFormatRegistry(context)
    }

    @Provides
    @Singleton
    fun provideSpoofDetector(bankFormatRegistry: BankFormatRegistry): SpoofDetector {
        return SpoofDetector(bankFormatRegistry)
    }

    @Provides
    @Singleton
    fun provideBankSmsParser(
        bankFormatRegistry: BankFormatRegistry,
        spoofDetector: SpoofDetector
    ): BankSmsParser {
        return BankSmsParser(bankFormatRegistry, spoofDetector)
    }

    @Provides
    @Singleton
    fun provideUserPreferencesManager(
        @ApplicationContext context: Context
    ): UserPreferencesManager {
        return UserPreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideSmsRepository(
        @ApplicationContext context: Context,
        parsedSmsDao: ParsedSmsDao,
        bankSmsParser: BankSmsParser
    ): SmsRepository {
        return SmsRepository(context, parsedSmsDao, bankSmsParser)
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(
        transactionDao: TransactionDao
    ): TransactionRepository {
        return TransactionRepository(transactionDao)
    }

    @Provides
    @Singleton
    fun provideSecurityRepository(
        securityEventDao: SecurityEventDao
    ): SecurityRepository {
        return SecurityRepository(securityEventDao)
    }
}

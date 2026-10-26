package com.bankiyemonitor.app.worker

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object AlertScheduler {

    private const val WORK_NAME = "BankiyeSummaryWorker"

    fun scheduleSummary(context: Context, frequency: String) {
        val workManager = WorkManager.getInstance(context)

        val (repeatInterval, timeUnit) = when (frequency.uppercase()) {
            "DAILY" -> Pair(1L, TimeUnit.DAYS)
            "WEEKLY" -> Pair(7L, TimeUnit.DAYS)
            "MONTHLY" -> Pair(30L, TimeUnit.DAYS)
            "YEARLY" -> Pair(365L, TimeUnit.DAYS)
            else -> Pair(7L, TimeUnit.DAYS)
        }

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val inputData = Data.Builder()
            .putString("PERIOD_TYPE", frequency)
            .build()

        val periodicWork = PeriodicWorkRequestBuilder<SummaryWorker>(repeatInterval, timeUnit)
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWork
        )
    }

    fun cancelSummary(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}

package com.bankiyemonitor.app.ui.screens.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bankiyemonitor.app.data.local.dao.ParsedSmsDao
import com.bankiyemonitor.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class SummaryUiState(
    val isLoading: Boolean = true,
    val periodType: String = "WEEKLY",
    val totalSent: Double = 0.0,
    val totalReceived: Double = 0.0,
    val transactionCount: Int = 0,
    val loginAttempts: Int = 0,
    val failedLogins: Int = 0,
    val passwordResets: Int = 0,
    val deviceChanges: Int = 0,
    val spoofedMessages: Int = 0,
    val highRiskEvents: Int = 0,
    val topCounterparties: List<Pair<String, Int>> = emptyList()
)

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val parsedSmsDao: ParsedSmsDao,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    fun loadSummary(periodType: String) {
        viewModelScope.launch {
            val now = LocalDate.now()
            val periodStart = when (periodType) {
                "DAILY" -> now
                "WEEKLY" -> now.minusDays((now.dayOfWeek.value - 1).toLong())
                "MONTHLY" -> now.withDayOfMonth(1)
                "YEARLY" -> now.withDayOfYear(1)
                else -> now.minusDays(7)
            }

            val startTime = periodStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endTime = System.currentTimeMillis()

            val totalSent = parsedSmsDao.getTotalDebited(startTime, endTime) ?: 0.0
            val totalReceived = parsedSmsDao.getTotalCredited(startTime, endTime) ?: 0.0
            val loginAttempts = parsedSmsDao.getLoginCount(startTime, endTime)
            val failedLogins = parsedSmsDao.getFailedLoginCount(startTime, endTime)
            val passwordResets = parsedSmsDao.getPasswordResetCount(startTime, endTime)
            val deviceChanges = parsedSmsDao.getDeviceChangeCount(startTime, endTime)
            val spoofedMessages = parsedSmsDao.getSpoofedCount(startTime, endTime)
            val highRiskEvents = parsedSmsDao.getHighRiskCount(startTime, endTime)
            val totalCount = parsedSmsDao.getTotalCount()

            _uiState.value = SummaryUiState(
                isLoading = false,
                periodType = periodType,
                totalSent = totalSent,
                totalReceived = totalReceived,
                transactionCount = totalCount,
                loginAttempts = loginAttempts,
                failedLogins = failedLogins,
                passwordResets = passwordResets,
                deviceChanges = deviceChanges,
                spoofedMessages = spoofedMessages,
                highRiskEvents = highRiskEvents
            )
        }
    }
}

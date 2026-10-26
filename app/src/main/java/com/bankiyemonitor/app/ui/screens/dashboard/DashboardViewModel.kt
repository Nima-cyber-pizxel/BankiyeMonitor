package com.bankiyemonitor.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bankiyemonitor.app.data.local.entity.ParsedSmsEntity
import com.bankiyemonitor.app.data.repository.SmsRepository
import com.bankiyemonitor.app.data.repository.SecurityRepository
import com.bankiyemonitor.app.data.local.dao.ParsedSmsDao
import com.bankiyemonitor.app.data.sample.SampleDataSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val totalSent: Double = 0.0,
    val totalReceived: Double = 0.0,
    val loginAttempts: Int = 0,
    val failedLogins: Int = 0,
    val passwordResets: Int = 0,
    val deviceChanges: Int = 0,
    val spoofedMessages: Int = 0,
    val highRiskEvents: Int = 0,
    val unresolvedSecurityEvents: Int = 0,
    val recentTransactions: List<ParsedSmsEntity> = emptyList(),
    val recentAlerts: List<ParsedSmsEntity> = emptyList(),
    val totalTransactions: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val smsRepository: SmsRepository,
    private val securityRepository: SecurityRepository,
    private val parsedSmsDao: ParsedSmsDao,
    private val sampleDataSeeder: SampleDataSeeder
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            val now = LocalDate.now()
            val weekStart = now.minusDays((now.dayOfWeek.value - 1).toLong())
            val startTime = weekStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endTime = System.currentTimeMillis()

            val totalSent = parsedSmsDao.getTotalDebited(startTime, endTime) ?: 0.0
            val totalReceived = parsedSmsDao.getTotalCredited(startTime, endTime) ?: 0.0

            val loginAttempts = parsedSmsDao.getLoginCount(startTime, endTime)
            val failedLogins = parsedSmsDao.getFailedLoginCount(startTime, endTime)
            val passwordResets = parsedSmsDao.getPasswordResetCount(startTime, endTime)
            val deviceChanges = parsedSmsDao.getDeviceChangeCount(startTime, endTime)
            val spoofedMessages = parsedSmsDao.getSpoofedCount(startTime, endTime)
            val highRiskEvents = parsedSmsDao.getHighRiskCount(startTime, endTime)
            val totalTransactions = parsedSmsDao.getTotalCount()

            combine(
                smsRepository.getAllParsedSms(),
                smsRepository.getHighRiskSms(),
                securityRepository.getUnresolvedCount()
            ) { allSms, highRiskSms, unresolvedCount ->
                _uiState.value = DashboardUiState(
                    isLoading = false,
                    totalSent = totalSent,
                    totalReceived = totalReceived,
                    loginAttempts = loginAttempts,
                    failedLogins = failedLogins,
                    passwordResets = passwordResets,
                    deviceChanges = deviceChanges,
                    spoofedMessages = spoofedMessages,
                    highRiskEvents = highRiskEvents,
                    unresolvedSecurityEvents = unresolvedCount,
                    recentTransactions = allSms.take(5),
                    recentAlerts = highRiskSms.take(5),
                    totalTransactions = totalTransactions
                )
            }.collect()
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            smsRepository.scanExistingSms()
            loadDashboardData()
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            sampleDataSeeder.seedSampleData()
            loadDashboardData()
        }
    }
}

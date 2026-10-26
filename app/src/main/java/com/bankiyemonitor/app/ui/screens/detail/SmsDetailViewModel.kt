package com.bankiyemonitor.app.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bankiyemonitor.app.data.local.dao.ParsedSmsDao
import com.bankiyemonitor.app.data.local.entity.ParsedSmsEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SmsDetailUiState(
    val sms: ParsedSmsEntity? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class SmsDetailViewModel @Inject constructor(
    private val parsedSmsDao: ParsedSmsDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmsDetailUiState())
    val uiState: StateFlow<SmsDetailUiState> = _uiState.asStateFlow()

    fun loadSms(smsId: Long) {
        viewModelScope.launch {
            val sms = parsedSmsDao.getSmsById(smsId)
            _uiState.value = SmsDetailUiState(sms = sms, isLoading = false)
            sms?.let { parsedSmsDao.markAsRead(it.id) }
        }
    }
}

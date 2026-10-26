package com.bankiyemonitor.app.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bankiyemonitor.app.data.local.entity.ParsedSmsEntity
import com.bankiyemonitor.app.data.repository.SmsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionsUiState(
    val isLoading: Boolean = true,
    val transactions: List<ParsedSmsEntity> = emptyList(),
    val currentFilter: String = "ALL"
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val smsRepository: SmsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    private val _filter = MutableStateFlow("ALL")

    init {
        viewModelScope.launch {
            _filter.collectLatest { filter ->
                val flow = when (filter) {
                    "CREDIT" -> smsRepository.getSmsByType("CREDIT")
                    "DEBIT" -> smsRepository.getSmsByType("DEBIT")
                    "LOGIN" -> smsRepository.getSmsByType("LOGIN")
                    "SECURITY" -> smsRepository.getHighRiskSms()
                    else -> smsRepository.getAllParsedSms()
                }

                flow.collect { transactions ->
                    _uiState.value = TransactionsUiState(
                        isLoading = false,
                        transactions = transactions,
                        currentFilter = filter
                    )
                }
            }
        }
    }

    fun setFilter(filter: String) {
        _filter.value = filter
    }
}

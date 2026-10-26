package com.bankiyemonitor.app.ui.screens.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bankiyemonitor.app.data.local.entity.SecurityEventEntity
import com.bankiyemonitor.app.data.repository.SecurityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SecurityUiState(
    val isLoading: Boolean = true,
    val events: List<SecurityEventEntity> = emptyList(),
    val unresolvedCount: Int = 0
)

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val securityRepository: SecurityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                securityRepository.getAllEvents(),
                securityRepository.getUnresolvedCount()
            ) { events, unresolvedCount ->
                SecurityUiState(
                    isLoading = false,
                    events = events,
                    unresolvedCount = unresolvedCount
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun resolveEvent(eventId: Long) {
        viewModelScope.launch {
            securityRepository.resolveEvent(eventId)
        }
    }
}

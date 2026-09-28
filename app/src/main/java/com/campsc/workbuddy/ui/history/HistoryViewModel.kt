package com.campsc.workbuddy.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campsc.workbuddy.data.SessionSummary
import com.campsc.workbuddy.data.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HistoryUiState(
    val sessions: List<SessionSummary> = emptyList(),
    val isLoading: Boolean = true,
)

class HistoryViewModel(repository: WorkoutRepository) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = repository.observeHistory()
        .map { sessions -> HistoryUiState(sessions = sessions, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState(),
        )
}

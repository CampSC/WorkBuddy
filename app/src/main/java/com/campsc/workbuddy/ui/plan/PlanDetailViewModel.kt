package com.campsc.workbuddy.ui.plan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.campsc.workbuddy.data.DayWithExercises
import com.campsc.workbuddy.data.Plan
import com.campsc.workbuddy.data.WorkoutRepository
import com.campsc.workbuddy.ui.PlanDetailRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlanDetailUiState(
    val plan: Plan? = null,
    val days: List<DayWithExercises> = emptyList(),
    val isLoading: Boolean = true,
)

class PlanDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: WorkoutRepository,
) : ViewModel() {

    // O id do plano vem da rota com que navegámos para aqui: PlanDetailRoute(planId).
    private val planId: Long = savedStateHandle.toRoute<PlanDetailRoute>().planId

    // combine junta dois Flows: sempre que um deles muda, calcula um estado novo.
    val uiState: StateFlow<PlanDetailUiState> = combine(
        repository.observePlan(planId),
        repository.observeDaysWithExercises(planId),
    ) { plan, days ->
        PlanDetailUiState(plan = plan, days = days, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlanDetailUiState(),
    )

    fun renamePlan(newName: String) {
        val plan = uiState.value.plan ?: return
        viewModelScope.launch { repository.renamePlan(plan, newName) }
    }

    fun deletePlan(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deletePlan(planId)
            onDeleted()
        }
    }

    fun addDay(name: String) {
        viewModelScope.launch { repository.addDay(planId, name) }
    }

    fun startSession(dayId: Long, onStarted: (sessionId: Long) -> Unit) {
        viewModelScope.launch {
            val sessionId = repository.startSession(dayId)
            onStarted(sessionId)
        }
    }
}

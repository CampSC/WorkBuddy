package com.campsc.workbuddy.ui.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.campsc.workbuddy.data.SetLog
import com.campsc.workbuddy.data.WorkoutRepository
import com.campsc.workbuddy.data.WorkoutSession
import com.campsc.workbuddy.ui.SessionRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Um exercício da sessão com as suas séries. */
data class ExerciseLog(
    val name: String,
    val position: Int,
    val sets: List<SetLog>,
)

data class SessionUiState(
    val session: WorkoutSession? = null,
    val exercises: List<ExerciseLog> = emptyList(),
    val isLoading: Boolean = true,
)

class SessionViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: WorkoutRepository,
) : ViewModel() {

    private val sessionId: Long = savedStateHandle.toRoute<SessionRoute>().sessionId

    val uiState: StateFlow<SessionUiState> = combine(
        repository.observeSession(sessionId),
        repository.observeSetLogs(sessionId),
    ) { session, setLogs ->
        SessionUiState(
            session = session,
            exercises = groupByExercise(setLogs),
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SessionUiState(),
    )

    // A base de dados devolve uma lista "plana" de séries (como numa folha de
    // Excel). Aqui agrupamo-las por exercício para as mostrar em cartões.
    private fun groupByExercise(setLogs: List<SetLog>): List<ExerciseLog> =
        setLogs
            .groupBy { it.exercisePosition to it.exerciseName }
            .map { (key, sets) ->
                val (position, name) = key
                ExerciseLog(name = name, position = position, sets = sets)
            }

    fun updateReps(setLogId: Long, reps: Int?) {
        viewModelScope.launch { repository.updateReps(setLogId, reps) }
    }

    fun updateWeight(setLogId: Long, weightKg: Double?) {
        viewModelScope.launch { repository.updateWeight(setLogId, weightKg) }
    }

    fun deleteSession(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            onDeleted()
        }
    }
}

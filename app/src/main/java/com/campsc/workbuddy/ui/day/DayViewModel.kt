package com.campsc.workbuddy.ui.day

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.campsc.workbuddy.data.Exercise
import com.campsc.workbuddy.data.PlanDay
import com.campsc.workbuddy.data.WorkoutRepository
import com.campsc.workbuddy.ui.DayRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DayUiState(
    val day: PlanDay? = null,
    val exercises: List<Exercise> = emptyList(),
    val isLoading: Boolean = true,
)

class DayViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: WorkoutRepository,
) : ViewModel() {

    private val dayId: Long = savedStateHandle.toRoute<DayRoute>().dayId

    val uiState: StateFlow<DayUiState> = combine(
        repository.observeDay(dayId),
        repository.observeExercises(dayId),
    ) { day, exercises ->
        DayUiState(day = day, exercises = exercises, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DayUiState(),
    )

    fun renameDay(newName: String) {
        val day = uiState.value.day ?: return
        viewModelScope.launch { repository.renameDay(day, newName) }
    }

    fun deleteDay(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteDay(dayId)
            onDeleted()
        }
    }

    fun addExercise(input: ExerciseInput) {
        viewModelScope.launch {
            repository.addExercise(
                dayId = dayId,
                name = input.name,
                sets = input.sets,
                reps = input.reps,
                weightKg = input.weightKg,
                notes = input.notes,
            )
        }
    }

    fun updateExercise(exercise: Exercise, input: ExerciseInput) {
        viewModelScope.launch {
            // copy() cria uma cópia do objeto só com estes campos alterados.
            repository.updateExercise(
                exercise.copy(
                    name = input.name,
                    sets = input.sets,
                    reps = input.reps,
                    weightKg = input.weightKg,
                    notes = input.notes,
                )
            )
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch { repository.deleteExercise(exercise) }
    }

    fun startSession(onStarted: (sessionId: Long) -> Unit) {
        viewModelScope.launch {
            val sessionId = repository.startSession(dayId)
            onStarted(sessionId)
        }
    }
}

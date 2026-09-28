package com.campsc.workbuddy.ui.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campsc.workbuddy.data.PlanSummary
import com.campsc.workbuddy.data.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Tudo o que o ecrã precisa para se desenhar. */
data class PlansUiState(
    val plans: List<PlanSummary> = emptyList(),
    val isLoading: Boolean = true,
)

/**
 * O ViewModel guarda o estado do ecrã e trata das ações do utilizador.
 * Sobrevive a rodar o ecrã (ao contrário das funções @Composable).
 */
class PlansViewModel(private val repository: WorkoutRepository) : ViewModel() {

    // stateIn transforma o Flow da base de dados num StateFlow, que tem sempre
    // um "valor atual" que o ecrã pode ler. WhileSubscribed(5_000): se o ecrã
    // deixar de estar visível durante 5s, deixamos de ouvir a base de dados.
    val uiState: StateFlow<PlansUiState> = repository.observePlans()
        .map { plans -> PlansUiState(plans = plans, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlansUiState(),
        )

    fun createPlan(name: String, numberOfDays: Int, onCreated: (planId: Long) -> Unit) {
        // viewModelScope.launch corre o código numa corrotina: podemos chamar
        // funções "suspend" (como as da base de dados) sem bloquear o ecrã.
        viewModelScope.launch {
            val planId = repository.createPlan(name, numberOfDays)
            onCreated(planId)
        }
    }
}

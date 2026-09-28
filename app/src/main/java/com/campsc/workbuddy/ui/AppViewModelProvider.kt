package com.campsc.workbuddy.ui

import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.campsc.workbuddy.ui.day.DayViewModel
import com.campsc.workbuddy.ui.history.HistoryViewModel
import com.campsc.workbuddy.ui.plan.PlanDetailViewModel
import com.campsc.workbuddy.ui.plans.PlansViewModel
import com.campsc.workbuddy.ui.session.SessionViewModel
import com.campsc.workbuddy.workBuddyApplication

/**
 * "Fábrica" que sabe criar todos os ViewModels da app, entregando a cada um
 * aquilo de que precisa (o repositório e, se for o caso, o SavedStateHandle
 * com os argumentos da rota).
 *
 * Nos ecrãs usa-se assim: viewModel(factory = AppViewModelProvider.Factory)
 *
 * Em apps maiores isto costuma ser feito por uma biblioteca de injeção de
 * dependências (Hilt, Koin...). Aqui fazemos à mão para se perceber o que se passa.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            PlansViewModel(workBuddyApplication().repository)
        }
        initializer {
            PlanDetailViewModel(createSavedStateHandle(), workBuddyApplication().repository)
        }
        initializer {
            DayViewModel(createSavedStateHandle(), workBuddyApplication().repository)
        }
        initializer {
            SessionViewModel(createSavedStateHandle(), workBuddyApplication().repository)
        }
        initializer {
            HistoryViewModel(workBuddyApplication().repository)
        }
    }
}

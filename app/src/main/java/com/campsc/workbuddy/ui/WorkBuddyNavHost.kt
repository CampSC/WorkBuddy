package com.campsc.workbuddy.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.campsc.workbuddy.ui.day.DayScreen
import com.campsc.workbuddy.ui.history.HistoryScreen
import com.campsc.workbuddy.ui.plan.PlanDetailScreen
import com.campsc.workbuddy.ui.plans.PlansScreen
import com.campsc.workbuddy.ui.session.SessionScreen
import kotlinx.serialization.Serializable

/*
 * Rotas: cada ecrã da app tem uma rota. As que levam dados (p.ex. o id do
 * plano a mostrar) são data classes; o ViewModel de cada ecrã lê esses dados
 * com savedStateHandle.toRoute<...>().
 */

@Serializable
data object PlansRoute

@Serializable
data class PlanDetailRoute(val planId: Long)

@Serializable
data class DayRoute(val dayId: Long)

@Serializable
data class SessionRoute(val sessionId: Long)

@Serializable
data object HistoryRoute

/**
 * O mapa da app: que ecrã mostrar para cada rota e para onde ir a seguir.
 *
 * Repara que os ecrãs não conhecem o navController: só recebem funções como
 * onPlanClick. Assim cada ecrã é independente e mais fácil de testar/reutilizar.
 */
@Composable
fun WorkBuddyNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = PlansRoute) {

        composable<PlansRoute> {
            PlansScreen(
                onPlanClick = { planId -> navController.navigate(PlanDetailRoute(planId)) },
                onHistoryClick = { navController.navigate(HistoryRoute) },
            )
        }

        composable<PlanDetailRoute> {
            PlanDetailScreen(
                onBack = { navController.navigateUp() },
                onDayClick = { dayId -> navController.navigate(DayRoute(dayId)) },
                onSessionStarted = { sessionId -> navController.navigate(SessionRoute(sessionId)) },
            )
        }

        composable<DayRoute> {
            DayScreen(
                onBack = { navController.navigateUp() },
                onSessionStarted = { sessionId -> navController.navigate(SessionRoute(sessionId)) },
            )
        }

        composable<SessionRoute> {
            SessionScreen(
                onBack = { navController.navigateUp() },
            )
        }

        composable<HistoryRoute> {
            HistoryScreen(
                onBack = { navController.navigateUp() },
                onSessionClick = { sessionId -> navController.navigate(SessionRoute(sessionId)) },
            )
        }
    }
}

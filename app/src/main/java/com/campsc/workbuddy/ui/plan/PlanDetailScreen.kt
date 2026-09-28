package com.campsc.workbuddy.ui.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campsc.workbuddy.data.DayWithExercises
import com.campsc.workbuddy.data.Exercise
import com.campsc.workbuddy.data.PlanDay
import com.campsc.workbuddy.ui.AppViewModelProvider
import com.campsc.workbuddy.ui.components.BackButton
import com.campsc.workbuddy.ui.components.ConfirmDeleteDialog
import com.campsc.workbuddy.ui.components.TextInputDialog
import com.campsc.workbuddy.ui.describeTarget

/** Um plano: a lista dos seus dias, cada um com o resumo dos exercícios. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailScreen(
    onBack: () -> Unit,
    onDayClick: (dayId: Long) -> Unit,
    onSessionStarted: (sessionId: Long) -> Unit,
    viewModel: PlanDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Que diálogo está aberto (se algum).
    var showRenameDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showAddDayDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.plan?.name ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = { BackButton(onClick = onBack) },
                actions = {
                    IconButton(onClick = { showRenameDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Mudar o nome do plano")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Apagar plano")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDayDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Adicionar dia") },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(uiState.days, key = { it.day.id }) { dayWithExercises ->
                DayCard(
                    dayWithExercises = dayWithExercises,
                    onClick = { onDayClick(dayWithExercises.day.id) },
                    onStartClick = { viewModel.startSession(dayWithExercises.day.id, onSessionStarted) },
                )
            }
        }
    }

    if (showRenameDialog) {
        TextInputDialog(
            title = "Mudar o nome do plano",
            label = "Nome",
            initialValue = uiState.plan?.name ?: "",
            confirmText = "Guardar",
            onConfirm = { newName ->
                showRenameDialog = false
                viewModel.renamePlan(newName)
            },
            onDismiss = { showRenameDialog = false },
        )
    }

    if (showAddDayDialog) {
        TextInputDialog(
            title = "Novo dia de treino",
            label = "Nome (ex.: Pernas)",
            initialValue = "Dia ${uiState.days.size + 1}",
            confirmText = "Adicionar",
            onConfirm = { name ->
                showAddDayDialog = false
                viewModel.addDay(name)
            },
            onDismiss = { showAddDayDialog = false },
        )
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title = "Apagar plano?",
            message = "Os dias e exercícios deste plano vão ser apagados. " +
                "Os treinos que já fizeste continuam no histórico.",
            onConfirm = {
                showDeleteDialog = false
                viewModel.deletePlan(onDeleted = onBack)
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun DayCard(
    dayWithExercises: DayWithExercises,
    onClick: () -> Unit,
    onStartClick: () -> Unit,
) {
    val exercises = dayWithExercises.exercises.sortedBy { it.position }

    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(dayWithExercises.day.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))

            if (exercises.isEmpty()) {
                Text(
                    text = "Sem exercícios. Toca para adicionar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                exercises.forEach { exercise ->
                    Text(
                        text = "${exercise.name}  ·  ${describeTarget(exercise)}",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                FilledTonalButton(onClick = onStartClick, enabled = exercises.isNotEmpty()) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text("Treinar")
                }
            }
        }
    }
}

// As @Preview aparecem no painel "Split"/"Design" do Android Studio,
// sem teres de correr a app. Experimenta mudar os dados e ver o resultado.
@Preview
@Composable
private fun DayCardPreview() {
    DayCard(
        dayWithExercises = DayWithExercises(
            day = PlanDay(id = 1, planId = 1, name = "Dia 1 - Peito e tríceps", position = 0),
            exercises = listOf(
                Exercise(id = 1, dayId = 1, name = "Supino reto", sets = 4, reps = 8, weightKg = 60.0, position = 0),
                Exercise(id = 2, dayId = 1, name = "Flexões", sets = 3, reps = 15, weightKg = null, position = 1),
            ),
        ),
        onClick = {},
        onStartClick = {},
    )
}

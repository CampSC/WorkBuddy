package com.campsc.workbuddy.ui.day

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campsc.workbuddy.data.Exercise
import com.campsc.workbuddy.ui.AppViewModelProvider
import com.campsc.workbuddy.ui.components.BackButton
import com.campsc.workbuddy.ui.components.ConfirmDeleteDialog
import com.campsc.workbuddy.ui.components.EmptyState
import com.campsc.workbuddy.ui.components.TextInputDialog
import com.campsc.workbuddy.ui.describeTarget

/** Um dia de treino: a lista de exercícios, que podes adicionar, editar e apagar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayScreen(
    onBack: () -> Unit,
    onSessionStarted: (sessionId: Long) -> Unit,
    viewModel: DayViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showRenameDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDayDialog by rememberSaveable { mutableStateOf(false) }
    var showAddExerciseDialog by rememberSaveable { mutableStateOf(false) }
    // Aqui usamos remember (e não rememberSaveable) porque um Exercise não
    // pode ser guardado automaticamente. Se rodares o ecrã, o diálogo fecha.
    var exerciseToEdit by remember { mutableStateOf<Exercise?>(null) }
    var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.day?.name ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = { BackButton(onClick = onBack) },
                actions = {
                    IconButton(onClick = { showRenameDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Mudar o nome do dia")
                    }
                    IconButton(onClick = { showDeleteDayDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Apagar dia")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddExerciseDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar exercício")
            }
        },
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = { viewModel.startSession(onSessionStarted) },
                    enabled = uiState.exercises.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text("Começar treino")
                }
            }
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> Unit
            uiState.exercises.isEmpty() -> EmptyState(
                text = "Este dia ainda não tem exercícios.\nToca em + para adicionar.",
                modifier = Modifier.padding(innerPadding),
            )
            else -> LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(bottom = 88.dp),
            ) {
                items(uiState.exercises, key = { it.id }) { exercise ->
                    ExerciseItem(
                        exercise = exercise,
                        onClick = { exerciseToEdit = exercise },
                        onDeleteClick = { exerciseToDelete = exercise },
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    // ----- Diálogos -----

    if (showAddExerciseDialog) {
        ExerciseDialog(
            initial = null,
            onConfirm = { input ->
                showAddExerciseDialog = false
                viewModel.addExercise(input)
            },
            onDismiss = { showAddExerciseDialog = false },
        )
    }

    // "?.let" só executa o bloco se exerciseToEdit não for null.
    exerciseToEdit?.let { exercise ->
        ExerciseDialog(
            initial = exercise,
            onConfirm = { input ->
                exerciseToEdit = null
                viewModel.updateExercise(exercise, input)
            },
            onDismiss = { exerciseToEdit = null },
        )
    }

    exerciseToDelete?.let { exercise ->
        ConfirmDeleteDialog(
            title = "Apagar exercício?",
            message = "\"${exercise.name}\" vai ser removido deste dia.",
            onConfirm = {
                exerciseToDelete = null
                viewModel.deleteExercise(exercise)
            },
            onDismiss = { exerciseToDelete = null },
        )
    }

    if (showRenameDialog) {
        TextInputDialog(
            title = "Mudar o nome do dia",
            label = "Nome (ex.: Peito e tríceps)",
            initialValue = uiState.day?.name ?: "",
            confirmText = "Guardar",
            onConfirm = { newName ->
                showRenameDialog = false
                viewModel.renameDay(newName)
            },
            onDismiss = { showRenameDialog = false },
        )
    }

    if (showDeleteDayDialog) {
        ConfirmDeleteDialog(
            title = "Apagar dia?",
            message = "Os exercícios deste dia vão ser apagados. " +
                "Os treinos que já fizeste continuam no histórico.",
            onConfirm = {
                showDeleteDayDialog = false
                viewModel.deleteDay(onDeleted = onBack)
            },
            onDismiss = { showDeleteDayDialog = false },
        )
    }
}

@Composable
private fun ExerciseItem(
    exercise: Exercise,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(exercise.name) },
        supportingContent = {
            val target = describeTarget(exercise)
            Text(if (exercise.notes.isBlank()) target else "$target\n${exercise.notes}")
        },
        trailingContent = {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Apagar ${exercise.name}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

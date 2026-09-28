package com.campsc.workbuddy.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campsc.workbuddy.data.SetLog
import com.campsc.workbuddy.ui.AppViewModelProvider
import com.campsc.workbuddy.ui.components.BackButton
import com.campsc.workbuddy.ui.components.ConfirmDeleteDialog
import com.campsc.workbuddy.ui.formatDateTime
import com.campsc.workbuddy.ui.formatWeight
import com.campsc.workbuddy.ui.isValidIntInput
import com.campsc.workbuddy.ui.isValidWeightInput
import com.campsc.workbuddy.ui.parseWeight

/**
 * O ecrã do treino: para cada exercício, uma linha por série onde escreves
 * as repetições que fizeste (e o peso, se mudou).
 * Tudo é guardado automaticamente enquanto escreves.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    onBack: () -> Unit,
    viewModel: SessionViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val session = uiState.session
                    if (session != null) {
                        Column {
                            Text(session.dayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                text = "${session.planName} · ${formatDateTime(session.startedAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                navigationIcon = { BackButton(onClick = onBack) },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Apagar treino")
                    }
                },
            )
        },
        bottomBar = {
            BottomAppBar {
                // Os dados já estão guardados; "Terminar" só volta para trás.
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Terminar treino")
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                // Deixa espaço para o teclado, para os campos não ficarem tapados.
                .consumeWindowInsets(innerPadding)
                .imePadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // A key tem de ser única: usamos o id da 1.ª série de cada exercício.
            items(uiState.exercises, key = { it.sets.first().id }) { exercise ->
                ExerciseLogCard(
                    exercise = exercise,
                    onRepsChange = viewModel::updateReps,
                    onWeightChange = viewModel::updateWeight,
                )
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title = "Apagar treino?",
            message = "Todas as séries registadas neste treino vão ser apagadas.",
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteSession(onDeleted = onBack)
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun ExerciseLogCard(
    exercise: ExerciseLog,
    onRepsChange: (setLogId: Long, reps: Int?) -> Unit,
    onWeightChange: (setLogId: Long, weightKg: Double?) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(exercise.name, style = MaterialTheme.typography.titleMedium)

            // Cabeçalho da "tabela".
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TableHeader("Série", Modifier.width(48.dp))
                TableHeader("Reps", Modifier.weight(1f))
                TableHeader("Kg", Modifier.weight(1f))
                Spacer(Modifier.width(24.dp))
            }

            exercise.sets.forEach { setLog ->
                // key() ajuda o Compose a saber que cada linha é uma série diferente.
                key(setLog.id) {
                    SetRow(
                        setLog = setLog,
                        onRepsChange = { reps -> onRepsChange(setLog.id, reps) },
                        onWeightChange = { weightKg -> onWeightChange(setLog.id, weightKg) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TableHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
private fun SetRow(
    setLog: SetLog,
    onRepsChange: (Int?) -> Unit,
    onWeightChange: (Double?) -> Unit,
) {
    // O texto de cada campo vive aqui, no ecrã, e é enviado para a base de
    // dados a cada alteração. Se lêssemos o texto diretamente da base de dados,
    // o cursor saltava enquanto escreves.
    var repsText by rememberSaveable { mutableStateOf(setLog.reps?.toString() ?: "") }
    var weightText by rememberSaveable { mutableStateOf(formatWeight(setLog.weightKg)) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = setLog.setNumber.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(48.dp),
        )
        OutlinedTextField(
            value = repsText,
            onValueChange = { text ->
                if (isValidIntInput(text)) {
                    repsText = text
                    onRepsChange(text.toIntOrNull())
                }
            },
            // O objetivo aparece a cinzento até escreveres alguma coisa.
            placeholder = { Text(setLog.targetReps.toString()) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
            value = weightText,
            onValueChange = { text ->
                if (isValidWeightInput(text)) {
                    weightText = text
                    onWeightChange(parseWeight(text))
                }
            },
            placeholder = { Text("—") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            modifier = Modifier.weight(1f),
        )
        // Um visto quando a série já tem repetições registadas.
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = if (repsText.isEmpty()) null else "Série registada",
            tint = if (repsText.isEmpty()) {
                MaterialTheme.colorScheme.outlineVariant
            } else {
                MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.size(24.dp),
        )
    }
}

@Preview
@Composable
private fun ExerciseLogCardPreview() {
    ExerciseLogCard(
        exercise = ExerciseLog(
            name = "Supino reto",
            position = 0,
            sets = listOf(
                SetLog(1, 1, 1, "Supino reto", 0, setNumber = 1, targetReps = 8, reps = 8, weightKg = 60.0),
                SetLog(2, 1, 1, "Supino reto", 0, setNumber = 2, targetReps = 8, reps = 7, weightKg = 60.0),
                SetLog(3, 1, 1, "Supino reto", 0, setNumber = 3, targetReps = 8, reps = null, weightKg = 57.5),
            ),
        ),
        onRepsChange = { _, _ -> },
        onWeightChange = { _, _ -> },
    )
}

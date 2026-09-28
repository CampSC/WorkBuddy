package com.campsc.workbuddy.ui.plans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campsc.workbuddy.data.PlanSummary
import com.campsc.workbuddy.ui.AppViewModelProvider
import com.campsc.workbuddy.ui.components.EmptyState
import com.campsc.workbuddy.ui.components.NumberStepper

/** Ecrã inicial: a lista de planos de treino. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(
    onPlanClick: (planId: Long) -> Unit,
    onHistoryClick: () -> Unit,
    viewModel: PlansViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    // "by" + collectAsStateWithLifecycle: sempre que o uiState muda, o ecrã é
    // redesenhado ("recomposição") com os valores novos.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showNewPlanDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Os meus planos") },
                actions = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.Default.DateRange, contentDescription = "Histórico de treinos")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showNewPlanDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Novo plano")
            }
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> Unit // ainda a ler da base de dados: não mostra nada
            uiState.plans.isEmpty() -> EmptyState(
                text = "Ainda não tens planos.\nToca em + para criar o primeiro.",
                modifier = Modifier.padding(innerPadding),
            )
            else -> LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.plans, key = { it.id }) { plan ->
                    PlanCard(plan = plan, onClick = { onPlanClick(plan.id) })
                }
            }
        }
    }

    if (showNewPlanDialog) {
        NewPlanDialog(
            onDismiss = { showNewPlanDialog = false },
            onConfirm = { name, numberOfDays ->
                showNewPlanDialog = false
                viewModel.createPlan(name, numberOfDays, onCreated = onPlanClick)
            },
        )
    }
}

@Composable
private fun PlanCard(plan: PlanSummary, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(plan.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = if (plan.dayCount == 1) "1 dia" else "${plan.dayCount} dias",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NewPlanDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, numberOfDays: Int) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var numberOfDays by rememberSaveable { mutableIntStateOf(3) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo plano") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome (ex.: Push/Pull/Legs)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                Spacer(Modifier.height(16.dp))
                Text("Quantos dias de treino?", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                NumberStepper(
                    value = numberOfDays,
                    onValueChange = { numberOfDays = it },
                    range = 1..7,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), numberOfDays) },
                enabled = name.isNotBlank(),
            ) {
                Text("Criar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

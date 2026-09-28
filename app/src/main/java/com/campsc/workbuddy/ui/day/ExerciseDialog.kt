package com.campsc.workbuddy.ui.day

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.campsc.workbuddy.data.Exercise
import com.campsc.workbuddy.ui.formatWeight
import com.campsc.workbuddy.ui.isValidIntInput
import com.campsc.workbuddy.ui.isValidWeightInput
import com.campsc.workbuddy.ui.parseWeight

/** Os valores preenchidos no formulário de exercício, já validados. */
data class ExerciseInput(
    val name: String,
    val sets: Int,
    val reps: Int,
    val weightKg: Double?,
    val notes: String,
)

/**
 * Formulário para criar (initial = null) ou editar um exercício.
 *
 * Os campos são guardados como texto enquanto se escreve, e só são
 * convertidos em números quando se carrega em "Guardar".
 */
@Composable
fun ExerciseDialog(
    initial: Exercise?,
    onConfirm: (ExerciseInput) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initial?.name ?: "") }
    var sets by rememberSaveable { mutableStateOf(initial?.sets?.toString() ?: "3") }
    var reps by rememberSaveable { mutableStateOf(initial?.reps?.toString() ?: "10") }
    var weight by rememberSaveable { mutableStateOf(formatWeight(initial?.weightKg)) }
    var notes by rememberSaveable { mutableStateOf(initial?.notes ?: "") }

    // Converte o texto em números (null se não for válido).
    val setsValue = sets.toIntOrNull()?.takeIf { it in 1..20 }
    val repsValue = reps.toIntOrNull()?.takeIf { it in 1..999 }
    val weightValue = parseWeight(weight)
    val weightIsValid = weight.isBlank() || weightValue != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Novo exercício" else "Editar exercício") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercício") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sets,
                        onValueChange = { if (isValidIntInput(it)) sets = it },
                        label = { Text("Séries") },
                        singleLine = true,
                        isError = setsValue == null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { if (isValidIntInput(it)) reps = it },
                        label = { Text("Reps") },
                        singleLine = true,
                        isError = repsValue == null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { if (isValidWeightInput(it)) weight = it },
                        label = { Text("Kg") },
                        singleLine = true,
                        isError = !weightIsValid,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas (opcional)") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (setsValue != null && repsValue != null && weightIsValid) {
                        onConfirm(
                            ExerciseInput(
                                name = name.trim(),
                                sets = setsValue,
                                reps = repsValue,
                                weightKg = weightValue,
                                notes = notes.trim(),
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && setsValue != null && repsValue != null && weightIsValid,
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

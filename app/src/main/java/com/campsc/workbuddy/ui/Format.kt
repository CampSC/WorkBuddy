package com.campsc.workbuddy.ui

import com.campsc.workbuddy.data.Exercise
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/*
 * Funções pequenas para converter números/datas em texto e vice-versa.
 * Estão separadas da interface para serem fáceis de testar
 * (ver app/src/test/.../FormatTest.kt).
 */

/** 60.0 -> "60", 62.5 -> "62,5", null -> "". */
fun formatWeight(weightKg: Double?): String = when {
    weightKg == null -> ""
    weightKg % 1.0 == 0.0 -> weightKg.toLong().toString()
    else -> weightKg.toString().replace('.', ',')
}

/** Aceita "62,5" ou "62.5". Devolve null se o texto estiver vazio ou não for um peso válido. */
fun parseWeight(text: String): Double? =
    text.trim()
        .replace(',', '.')
        .toDoubleOrNull()
        ?.takeIf { it >= 0 }

/** Só deixa escrever números inteiros com até 3 algarismos (ex.: repetições, séries). */
fun isValidIntInput(text: String): Boolean =
    text.length <= 3 && text.all { it.isDigit() }

/** Só deixa escrever algarismos e, no máximo, uma vírgula ou ponto. */
fun isValidWeightInput(text: String): Boolean =
    text.length <= 6 &&
        text.all { it.isDigit() || it == ',' || it == '.' } &&
        text.count { it == ',' || it == '.' } <= 1

/** Ex.: "4 × 10 · 60 kg" ou "3 × 12" (sem peso). */
fun describeTarget(exercise: Exercise): String {
    val setsAndReps = "${exercise.sets} × ${exercise.reps}"
    return if (exercise.weightKg == null) setsAndReps else "$setsAndReps · ${formatWeight(exercise.weightKg)} kg"
}

// Usa o formato de data do telemóvel (em Portugal: "28/09/2026, 18:30").
private val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

fun formatDateTime(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(dateTimeFormatter)

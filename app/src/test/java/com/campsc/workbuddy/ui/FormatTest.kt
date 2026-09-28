package com.campsc.workbuddy.ui

import com.campsc.workbuddy.data.Exercise
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes unitários: correm no teu PC (sem emulador) e verificam que as
 * funções de Format.kt fazem o que devem.
 *
 * Para correr: botão direito nesta classe > Run 'FormatTest'.
 */
class FormatTest {

    @Test
    fun formatWeight_removesDecimalsFromWholeNumbers() {
        assertEquals("60", formatWeight(60.0))
    }

    @Test
    fun formatWeight_usesCommaAsDecimalSeparator() {
        assertEquals("62,5", formatWeight(62.5))
    }

    @Test
    fun formatWeight_isEmptyWhenThereIsNoWeight() {
        assertEquals("", formatWeight(null))
    }

    @Test
    fun parseWeight_acceptsCommaAndDot() {
        assertEquals(62.5, parseWeight("62,5")!!, 0.0)
        assertEquals(62.5, parseWeight("62.5")!!, 0.0)
    }

    @Test
    fun parseWeight_returnsNullForInvalidText() {
        assertNull(parseWeight(""))
        assertNull(parseWeight("abc"))
        assertNull(parseWeight("-5"))
    }

    @Test
    fun isValidIntInput_onlyAcceptsUpToThreeDigits() {
        assertTrue(isValidIntInput(""))
        assertTrue(isValidIntInput("12"))
        assertFalse(isValidIntInput("1234"))
        assertFalse(isValidIntInput("1a"))
    }

    @Test
    fun isValidWeightInput_acceptsOnlyOneDecimalSeparator() {
        assertTrue(isValidWeightInput("62,5"))
        assertFalse(isValidWeightInput("62,5,1"))
        assertFalse(isValidWeightInput("6.2,5"))
    }

    @Test
    fun describeTarget_showsSetsRepsAndWeight() {
        val exercise = Exercise(dayId = 1, name = "Supino", sets = 4, reps = 10, weightKg = 60.0, position = 0)
        assertEquals("4 × 10 · 60 kg", describeTarget(exercise))
    }

    @Test
    fun describeTarget_omitsWeightForBodyweightExercises() {
        val exercise = Exercise(dayId = 1, name = "Flexões", sets = 3, reps = 15, weightKg = null, position = 0)
        assertEquals("3 × 15", describeTarget(exercise))
    }
}

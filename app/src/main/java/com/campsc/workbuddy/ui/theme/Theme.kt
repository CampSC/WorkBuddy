package com.campsc.workbuddy.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Experimenta mudar estas cores e vê o resultado (em telemóveis com Android 12+
// desliga primeiro o dynamicColor, senão são usadas as cores do papel de parede).
private val LightColors = lightColorScheme(
    primary = Color(0xFF00695C),
    secondary = Color(0xFF4A635E),
    tertiary = Color(0xFF45617A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF80CBC4),
    secondary = Color(0xFFB1CCC6),
    tertiary = Color(0xFFADCAE6),
)

@Composable
fun WorkBuddyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // "Material You": usa as cores do papel de parede (Android 12+).
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}

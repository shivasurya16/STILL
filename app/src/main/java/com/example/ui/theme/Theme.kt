package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StillDarkColorScheme = darkColorScheme(
    primary = StillPrimary,
    onPrimary = Color(0xFF0B0F14),
    primaryContainer = StillElevated,
    onPrimaryContainer = StillPrimary,
    secondary = StillSkills,
    onSecondary = Color(0xFF0B0F14),
    secondaryContainer = StillElevated,
    onSecondaryContainer = StillSkills,
    tertiary = StillAcademics,
    onTertiary = Color(0xFF0B0F14),
    tertiaryContainer = StillElevated,
    onTertiaryContainer = StillAcademics,
    background = StillBackground,
    onBackground = StillTextPrimary,
    surface = StillCard,
    onSurface = StillTextPrimary,
    surfaceVariant = StillElevated,
    onSurfaceVariant = StillTextSecondary,
    outline = StillBorder,
    outlineVariant = StillElevated,
    error = StillError,
    onError = Color.White
)

object StillTokens {
    val background = StillBackground
    val card = StillCard
    val elevated = StillElevated
    val primary = StillPrimary
    val focus = StillFocus
    val academics = StillAcademics
    val skills = StillSkills
    val distraction = StillDistraction
    val error = StillError
    val textPrimary = StillTextPrimary
    val textSecondary = StillTextSecondary
    val border = StillBorder
}

@Composable
fun StillTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StillDarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    StillTheme(content = content)
}

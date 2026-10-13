package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LudoLightColorScheme = lightColorScheme(
    primary = LudoPrimaryLime,
    onPrimary = LudoOnPrimary,
    primaryContainer = LudoPillTabActive,
    onPrimaryContainer = LudoTextPrimary,
    secondary = LudoTextPrimary,
    onSecondary = LudoOnPrimary,
    tertiary = LudoAccentLime,
    background = LudoBackground,
    onBackground = LudoTextTitle,
    surface = LudoSurface,
    onSurface = LudoTextTitle,
    surfaceVariant = LudoSurfaceVariant,
    onSurfaceVariant = LudoTextSecondary,
    error = LudoErrorRed,
    outline = LudoBorder
)

private val LudoDarkColorScheme = darkColorScheme(
    primary = LudoPrimaryLime,
    onPrimary = LudoOnPrimary,
    primaryContainer = LudoTextPrimary,
    onPrimaryContainer = LudoPillTabActive,
    secondary = LudoAccentLime,
    onSecondary = LudoTextHeadingsDark,
    tertiary = LudoAccentLime,
    background = Color(0xFF111827),
    onBackground = Color(0xFFF9FAFB),
    surface = Color(0xFF1F2937),
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = Color(0xFF374151),
    onSurfaceVariant = Color(0xFF9CA3AF),
    error = LudoErrorRed,
    outline = Color(0xFF4B5563)
)

@Composable
fun LudoSquadTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // For branded consistency, we use the custom Ludo palette
    val colorScheme = if (darkTheme) LudoDarkColorScheme else LudoLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

// Alias for template backwards-compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    LudoSquadTheme(darkTheme = darkTheme, content = content)
}

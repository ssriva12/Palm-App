package com.palmlens.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Violet,
    onPrimary = Color(0xFF1A1030),
    primaryContainer = MidnightSurface2,
    onPrimaryContainer = VioletBright,
    secondary = Gold,
    onSecondary = Color(0xFF2A2010),
    secondaryContainer = Color(0xFF3A2F17),
    onSecondaryContainer = Gold,
    tertiary = VioletBright,
    background = Midnight,
    onBackground = Starlight,
    surface = MidnightSurface,
    onSurface = Starlight,
    surfaceVariant = MidnightSurface2,
    onSurfaceVariant = Muted,
    outline = OutlineDark,
    outlineVariant = Color(0xFF322A4E),
)

private val LightColors = lightColorScheme(
    primary = VioletDeep,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE3FF),
    onPrimaryContainer = VioletDeep,
    secondary = GoldDeep,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF7EBD2),
    onSecondaryContainer = Color(0xFF5A4413),
    tertiary = VioletDeep,
    background = Lavender,
    onBackground = Ink,
    surface = LavenderSurface,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEEE8FA),
    onSurfaceVariant = Color(0xFF5C5478),
    outline = OutlineLight,
    outlineVariant = Color(0xFFE0D8F2),
)

@Composable
fun PalmlensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}

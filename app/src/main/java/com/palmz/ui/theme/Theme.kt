package com.palmz.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColors = lightColorScheme(
    primary = Rust,
    onPrimary = OnRust,
    primaryContainer = RustSoft,
    onPrimaryContainer = OnRustSoft,
    secondary = Mustard,
    onSecondary = OnMustard,
    secondaryContainer = MustardSoft,
    onSecondaryContainer = OnMustardSoft,
    tertiary = Rust,
    background = Cream,
    onBackground = InkText,
    surface = Paper,
    onSurface = InkText,
    surfaceVariant = PaperSunk,
    onSurfaceVariant = InkTextSoft,
    outline = Edge,
    outlineVariant = EdgeSoft,
    error = Danger,
    onError = OnDanger,
    errorContainer = DangerSoft,
    onErrorContainer = OnDangerSoft,
)

private val DarkColors = darkColorScheme(
    primary = RustLift,
    onPrimary = OnRustLift,
    primaryContainer = RustLiftSoft,
    onPrimaryContainer = OnRustLiftSoft,
    secondary = MustardLift,
    onSecondary = OnMustardLift,
    secondaryContainer = MustardLiftSoft,
    onSecondaryContainer = OnMustardLiftSoft,
    tertiary = RustLift,
    background = Espresso,
    onBackground = InkOnDark,
    surface = EspressoRaised,
    onSurface = InkOnDark,
    surfaceVariant = EspressoSunk,
    onSurfaceVariant = InkOnDarkSoft,
    outline = EdgeDark,
    outlineVariant = EdgeSoftDark,
    error = DangerLift,
    onError = OnDangerLift,
    errorContainer = DangerLiftSoft,
    onErrorContainer = OnDangerLiftSoft,
)

@Composable
fun PalmlensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val clay = ClayColors(ink = if (darkTheme) InkOnDark else ShadowLight)
    CompositionLocalProvider(LocalClayColors provides clay) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = Typography,
            content = content,
        )
    }
}

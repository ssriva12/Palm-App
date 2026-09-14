package com.palmlens.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp

/** Card ink: the outline colour, also used to tint the fill a touch on press. */
@Immutable
data class ClayColors(val ink: Color)

val LocalClayColors = staticCompositionLocalOf { ClayColors(ink = ShadowLight) }

val ClayShape: Shape = RoundedCornerShape(14.dp)
val ClayShapeSmall: Shape = RoundedCornerShape(10.dp)

private val BorderWidth = .5.dp

/**
 * Flat retro-poster surface: solid fill and a firm ink outline, no elevation shadow.
 * [pressed] tints the fill toward ink for tap feedback.
 */
@Composable
fun Modifier.clay(
    shape: Shape = ClayShape,
    fill: Color = MaterialTheme.colorScheme.surface,
    pressed: Boolean = false,
    bordered: Boolean = true,
): Modifier {
    val ink = LocalClayColors.current.ink
    val pressAmount by animateFloatAsState(if (pressed) 0.12f else 0f, tween(100), label = "pressDim")
    return this
        .clip(shape)
        .background(lerp(fill, ink, pressAmount))
        .then(if (bordered) Modifier.border(BorderWidth, ink, shape) else Modifier)
}

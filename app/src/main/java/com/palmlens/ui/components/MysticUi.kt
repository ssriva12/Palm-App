package com.palmlens.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.palmlens.R
import com.palmlens.ui.theme.ClayShape
import com.palmlens.ui.theme.ClayShapeSmall
import com.palmlens.ui.theme.clay
import kotlin.math.hypot
import com.palmlens.ui.theme.Spacing

/** Flat ground with a faint sunburst emblem behind the header. Clay surfaces do the rest. */
@Composable
fun MysticBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val background = MaterialTheme.colorScheme.background
    val rayColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
    Box(
        modifier
            .fillMaxSize()
            .background(background),
    ) {
        Sunburst(rayColor)
        content()
    }
}

@Composable
private fun BoxScope.Sunburst(rayColor: Color) {
    Canvas(Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val rayLength = hypot(size.width, size.height) / 2f
        val rayWidth = 16.dp.toPx()
        val rayCount = 48
        for (i in 0 until rayCount) {
            if (i % 2 == 0) continue
            rotate((360f / rayCount) * i, pivot = center) {
                drawRect(
                    color = rayColor,
                    topLeft = Offset(center.x - rayWidth / 2f, center.y),
                    size = Size(rayWidth, rayLength),
                )
            }
        }
    }
}

/**
 * No system app bar — back/title/actions render as a plain row directly above the content
 * instead of a pinned Material `TopAppBar`, so there's no app-bar chrome (elevation, scroll
 * behaviour, forced height) anywhere in the app. [titleContent], when set, replaces the
 * plain [title] text — e.g. a two-line greeting.
 */
@Composable
fun MysticScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    titleContent: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    MysticBackground(modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            // The outer per-tab Scaffold (PalmlensApp) already reserves space for both the
            // system bars and the bottom nav bar. Scaffold's own default re-applies
            // WindowInsets.safeDrawing on top of that, double-padding the bottom of every
            // screen — zero it out here since it's handled once, at the outer level.
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = bottomBar,
        ) { pad ->
            Column(Modifier.padding(pad).fillMaxSize()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.space8, vertical = Spacing.space8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.cd_navigate_back),
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                    Box(Modifier.weight(1f).padding(horizontal = Spacing.space8)) {
                        if (titleContent != null) {
                            titleContent()
                        } else if (title.isNotEmpty()) {
                            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                    actions()
                }
                content(PaddingValues(0.dp))
            }
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    bordered: Boolean = true,
    onClick: () -> Unit,
) {
    ClayButton(
        text = text,
        fill = MaterialTheme.colorScheme.primary,
        textColor = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier,
        enabled = enabled,
        bordered = bordered,
        onClick = onClick,
    )
}

/** [compact] shrinks padding/height/type for tight spots like a top app bar action. */
@Composable
fun SecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    compact: Boolean = false,
    onClick: () -> Unit,
) {
    ClayButton(
        text = text,
        fill = MaterialTheme.colorScheme.surface,
        textColor = MaterialTheme.colorScheme.primary,
        modifier = modifier,
        enabled = enabled,
        compact = compact,
        onClick = onClick,
    )
}

@Composable
private fun ClayButton(
    text: String,
    fill: Color,
    textColor: Color,
    modifier: Modifier,
    enabled: Boolean,
    compact: Boolean = false,
    bordered: Boolean = true,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        modifier
            .then(if (compact) Modifier else Modifier.heightIn(min = 52.dp))
            .alpha(if (enabled) 1f else 0.45f)
            .clay(ClayShapeSmall, fill = fill, pressed = pressed && enabled, bordered = bordered)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(
                horizontal = if (compact) Spacing.space14 else Spacing.space24,
                vertical = if (compact) Spacing.space8 else Spacing.space14,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
            color = textColor,
        )
    }
}

/** A tappable clay row with an optional selected ring. */
@Composable
fun SelectableRow(
    title: String,
    subtitle: String?,
    selected: Boolean,
    modifier: Modifier = Modifier,
    leading: String? = null,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Row(
        modifier
            .fillMaxWidth()
            .clay(
                ClayShapeSmall,
                fill = if (selected) cs.primaryContainer else cs.surface,
                pressed = pressed,
            )
            .clickable(interaction, indication = null, onClick = onClick)
            .padding(horizontal = Spacing.space16, vertical = Spacing.space14),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.space12),
    ) {
        if (leading != null) Text(leading, style = MaterialTheme.typography.titleLarge)
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
            }
        }
        if (selected) {
            Box(
                Modifier.size(22.dp).clip(CircleShape).background(cs.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = cs.onPrimary,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

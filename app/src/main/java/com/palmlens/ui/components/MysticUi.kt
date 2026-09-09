package com.palmlens.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.random.Random

/** Vertical gradient ground + a faint scatter of stars. The app's backdrop. */
@Composable
fun MysticBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val stars = remember {
        val r = Random(42)
        List(48) { Triple(r.nextFloat(), r.nextFloat(), r.nextFloat()) }
    }
    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(cs.background, cs.surface, cs.background))),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            stars.forEach { (fx, fy, fr) ->
                drawCircle(
                    color = cs.onBackground.copy(alpha = 0.05f + fr * 0.10f),
                    radius = (0.6f + fr * 1.8f).dp.toPx(),
                    center = Offset(fx * size.width, fy * size.height),
                )
            }
        }
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MysticScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    MysticBackground(modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(title, style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.semantics { contentDescription = "Navigate back" },
                            ) {
                                Text(
                                    "←",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                    },
                    actions = actions,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                )
            },
            content = content,
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(14.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(14.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** A tappable list row with an optional selected ring. */
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
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) cs.primaryContainer else cs.surface)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) cs.primary else cs.outlineVariant,
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                Modifier.size(20.dp).clip(CircleShape).background(cs.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "✓",
                    color = cs.onPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

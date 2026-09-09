package com.palmlens.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** The base surface used everywhere: rounded, subtly bordered, optionally tappable. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: Int = 16,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier
            .clip(shape)
            .background(cs.surface)
            .border(1.dp, cs.outlineVariant, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding.dp),
        content = content,
    )
}

@Composable
fun FeatureTile(
    emoji: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    GlassCard(modifier = modifier, onClick = onClick) {
        Text(emoji, fontSize = 30.sp)
        Spacer(Modifier.height(10.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier, contentPadding = 14) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier,
    )
}

/** Centered loading state with a mystic one-liner. */
@Composable
fun LoadingState(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(40.dp)
            .semantics { contentDescription = message },
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            androidx.compose.material3.CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

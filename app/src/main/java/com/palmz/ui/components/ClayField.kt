package com.palmz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.palmz.ui.theme.ClayShapeSmall
import com.palmz.ui.theme.Spacing

/**
 * Labelled text input as a recessed clay well: a soft filled trough with a hairline rim
 * that takes the primary colour on focus. No drop shadow — inputs sit in, not out.
 */
@Composable
fun ClayTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val cs = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()

    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
        Spacer(Modifier.height(Spacing.space6))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = singleLine,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = if (enabled) cs.onSurface else cs.onSurfaceVariant,
            ),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            cursorBrush = SolidColor(cs.primary),
            interactionSource = interaction,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                ClayWell(active = focused && enabled, dim = !enabled) {
                    if (value.isEmpty() && placeholder != null) {
                        Text(
                            placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = cs.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    }
                    inner()
                }
            },
        )
        if (supportingText != null) {
            Spacer(Modifier.height(Spacing.space4))
            Text(supportingText, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
        }
    }
}

/** Tappable clay well showing [value] or [placeholder] — backs the date / time pickers. */
@Composable
fun ClayFieldButton(
    label: String,
    value: String?,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
        Spacer(Modifier.height(Spacing.space6))
        ClayWell(
            active = pressed,
            dim = false,
            modifier = Modifier
                .clip(ClayShapeSmall)
                .clickable(interaction, indication = null, onClick = onClick),
        ) {
            Text(
                value ?: placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = if (value != null) cs.onSurface else cs.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun ClayWell(
    active: Boolean,
    dim: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .alpha(if (dim) 0.55f else 1f)
            .clip(ClayShapeSmall)
            .background(cs.surface)
            .border(1.5.dp, if (active) cs.primary else cs.outline, ClayShapeSmall)
            .padding(horizontal = Spacing.space16, vertical = Spacing.space14),
        contentAlignment = Alignment.CenterStart,
        content = content,
    )
}

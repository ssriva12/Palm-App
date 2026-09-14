package com.palmlens.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.palmlens.R
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun formatDate(d: LocalDate): String = d.format(DATE_FORMAT)

fun formatTime(t: LocalTime): String = t.format(TIME_FORMAT)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DobPickerButton(
    value: LocalDate?,
    label: String,
    modifier: Modifier = Modifier,
    onPicked: (LocalDate) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    ClayFieldButton(
        label = label,
        value = value?.let(::formatDate),
        placeholder = stringResource(R.string.pick_date_placeholder),
        onClick = { open = true },
        modifier = modifier,
    )
    if (open) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        // DatePicker reports UTC midnight; read it back the same way.
                        onPicked(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    open = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text(stringResource(R.string.cancel)) } },
        ) {
            DatePicker(state = state)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerButton(
    value: LocalTime?,
    label: String,
    modifier: Modifier = Modifier,
    onPicked: (LocalTime) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    ClayFieldButton(
        label = label,
        value = value?.let(::formatTime),
        placeholder = stringResource(R.string.pick_time_placeholder),
        onClick = { open = true },
        modifier = modifier,
    )
    if (open) {
        val state = rememberTimePickerState(
            initialHour = value?.hour ?: 12,
            initialMinute = value?.minute ?: 0,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    onPicked(LocalTime.of(state.hour, state.minute))
                    open = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text(stringResource(R.string.cancel)) } },
            title = { Text(stringResource(R.string.time_of_birth_label)) },
            text = { TimeInput(state = state) },
        )
    }
}

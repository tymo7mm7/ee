@file:OptIn(ExperimentalMaterial3Api::class)

package com.tymo.kalendarz.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import com.tymo.kalendarz.Occurrence
import com.tymo.kalendarz.PL
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

val HM: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun cap(s: String) = s.replaceFirstChar { it.uppercase() }
fun dayShort(d: LocalDate) = d.dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, PL).take(3)
fun dateLong(d: LocalDate): String = cap(d.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", PL)))
fun dateMedium(d: LocalDate): String = d.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", PL))

fun timeRange(o: Occurrence): String = when {
    o.event.allDay && o.multiDay -> "${dateMedium(o.firstDay)} – ${dateMedium(o.lastDay)}"
    o.event.allDay -> "Cały dzień"
    o.multiDay -> "${dateMedium(o.firstDay)} ${o.start.format(HM)} – ${dateMedium(o.end.toLocalDate())} ${o.end.format(HM)}"
    else -> "${o.start.format(HM)} – ${o.end.format(HM)}"
}

@Composable
fun DatePick(initial: LocalDate, onDismiss: () -> Unit, onPick: (LocalDate) -> Unit) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onPick(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } },
    ) { androidx.compose.material3.DatePicker(state = state) }
}

@Composable
fun TimePick(initial: LocalTime, onDismiss: () -> Unit, onPick: (LocalTime) -> Unit) {
    val state = rememberTimePickerState(initial.hour, initial.minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { TimePicker(state = state) },
        confirmButton = { TextButton(onClick = { onPick(LocalTime.of(state.hour, state.minute)); onDismiss() }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } },
    )
}

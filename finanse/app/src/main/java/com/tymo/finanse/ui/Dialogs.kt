@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.tymo.finanse.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tymo.finanse.Recurring
import com.tymo.finanse.Tx
import com.tymo.finanse.categories
import com.tymo.finanse.parseMoney
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.roundToInt

private fun amountText(gr: Long): String =
    if (gr == 0L) "" else if (gr % 100 == 0L) "${gr / 100}" else "%.2f".format(gr / 100.0).replace('.', ',')

@Composable
private fun TypeSwitch(income: Boolean, onChange: (Boolean) -> Unit) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        listOf(false to "⬇ Wydatek", true to "⬆ Przychód").forEachIndexed { i, (value, label) ->
            SegmentedButton(
                selected = income == value,
                onClick = { onChange(value) },
                shape = SegmentedButtonDefaults.itemShape(i, 2),
            ) { Text(label) }
        }
    }
}

@Composable
private fun CategoryPicker(income: Boolean, selected: String, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        categories.filter { it.income == income }.forEach { c ->
            FilterChip(
                selected = selected == c.id,
                onClick = { onSelect(c.id) },
                label = { Text("${c.emoji} ${c.label}") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = c.color.color().copy(alpha = 0.3f)),
            )
        }
    }
}

private fun firstCategory(income: Boolean) = categories.first { it.income == income }.id

@Composable
fun TxDialog(initial: Tx, isNew: Boolean, onDismiss: () -> Unit, onSave: (Tx) -> Unit, onDelete: () -> Unit) {
    var income by remember { mutableStateOf(initial.income) }
    var amount by remember { mutableStateOf(amountText(initial.amountGr)) }
    var cat by remember { mutableStateOf(initial.categoryId) }
    var note by remember { mutableStateOf(initial.note) }
    var date by remember { mutableStateOf(initial.localDate) }
    var picking by remember { mutableStateOf(false) }
    val gr = parseMoney(amount)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Nowa transakcja" else "Edytuj transakcję") },
        text = {
            Column(
                Modifier.heightIn(max = 540.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TypeSwitch(income) {
                    income = it
                    cat = firstCategory(it)
                }
                MoneyField("Kwota", amount, { amount = it })
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(60) },
                    label = { Text("Opis (opcjonalnie)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                AssistChip(
                    onClick = { picking = true },
                    label = { Text(dayHeader(date)) },
                    leadingIcon = { Icon(Icons.Default.DateRange, null) },
                )
                Text("Kategoria", style = MaterialTheme.typography.labelLarge)
                CategoryPicker(income, cat) { cat = it }
            }
        },
        confirmButton = {
            TextButton(enabled = gr != null, onClick = {
                onSave(initial.copy(amountGr = gr ?: 0, income = income, categoryId = cat, note = note.trim(), date = date.toString()))
            }) { Text("Zapisz") }
        },
        dismissButton = {
            Row {
                if (!isNew) TextButton(onClick = onDelete) { Text("Usuń", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text("Anuluj") }
            }
        },
    )

    if (picking) {
        val state = rememberDatePickerState(initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { picking = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
                    picking = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { picking = false }) { Text("Anuluj") } },
        ) { DatePicker(state = state) }
    }
}

@Composable
fun BudgetDialog(title: String, current: Long?, onDismiss: () -> Unit, onSave: (Long?) -> Unit) {
    var amount by remember { mutableStateOf(current?.let { amountText(it) } ?: "") }
    val gr = parseMoney(amount)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { MoneyField("Miesięczny limit", amount, { amount = it }) },
        confirmButton = { TextButton(enabled = gr != null, onClick = { onSave(gr) }) { Text("Zapisz") } },
        dismissButton = {
            Row {
                if (current != null) TextButton(onClick = { onSave(null) }) { Text("Usuń limit", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text("Anuluj") }
            }
        },
    )
}

@Composable
fun RecurringDialog(initial: Recurring, isNew: Boolean, onDismiss: () -> Unit, onSave: (Recurring) -> Unit, onDelete: () -> Unit) {
    var name by remember { mutableStateOf(initial.name) }
    var income by remember { mutableStateOf(initial.income) }
    var amount by remember { mutableStateOf(amountText(initial.amountGr)) }
    var cat by remember { mutableStateOf(initial.categoryId) }
    var day by remember { mutableFloatStateOf(initial.day.toFloat()) }
    val gr = parseMoney(amount)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Nowa stała pozycja" else "Edytuj stałą pozycję") },
        text = {
            Column(
                Modifier.heightIn(max = 540.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TypeSwitch(income) {
                    income = it
                    cat = if (it) "salary" else "bills"
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(40) },
                    label = { Text("Nazwa, np. Czynsz, Spotify") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                MoneyField("Kwota", amount, { amount = it })
                Text("Dzień miesiąca: ${day.roundToInt()}", style = MaterialTheme.typography.labelLarge)
                Slider(value = day, onValueChange = { day = it }, valueRange = 1f..28f, steps = 26)
                Text("Kategoria", style = MaterialTheme.typography.labelLarge)
                CategoryPicker(income, cat) { cat = it }
                if (isNew) {
                    Text(
                        if (day.roundToInt() <= LocalDate.now().dayOfMonth) "Pozycja za ten miesiąc zostanie dodana od razu."
                        else "Pierwsza pozycja doda się ${day.roundToInt()}. dnia tego miesiąca.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(enabled = gr != null && name.isNotBlank(), onClick = {
                onSave(initial.copy(name = name.trim(), income = income, amountGr = gr ?: 0, categoryId = cat, day = day.roundToInt()))
            }) { Text("Zapisz") }
        },
        dismissButton = {
            Row {
                if (!isNew) TextButton(onClick = onDelete) { Text("Usuń", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text("Anuluj") }
            }
        },
    )
}

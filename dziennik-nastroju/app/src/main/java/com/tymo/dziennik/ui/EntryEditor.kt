@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.tymo.dziennik.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tymo.dziennik.CRISIS_SYMPTOM
import com.tymo.dziennik.Entry
import com.tymo.dziennik.JournalData
import com.tymo.dziennik.energyLabel
import com.tymo.dziennik.moodEmoji
import com.tymo.dziennik.moodLabel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.roundToInt

@Composable
fun EntryEditor(
    initial: Entry,
    data: JournalData,
    onClose: () -> Unit,
    onSave: (Entry) -> Unit,
    onDelete: (String) -> Unit,
    onAddSymptom: (String) -> Unit,
) {
    var e by remember { mutableStateOf(initial) }
    var pickingDate by remember { mutableStateOf(false) }
    var addingSymptom by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val exists = data.entries.containsKey(e.date)

    BackHandler(onBack = onClose)

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = "Zamknij") }
                },
                title = { Text(if (exists) "Edycja wpisu" else "Nowy wpis") },
                actions = { TextButton(onClick = { onSave(e) }) { Text("Zapisz", fontWeight = FontWeight.Bold) } },
            )
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AssistChip(
                onClick = { pickingDate = true },
                label = { Text(longDate(e.localDate)) },
                leadingIcon = { Icon(Icons.Default.DateRange, null) },
            )

            SectionCard("Jak się dziś czujesz?") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    (1..5).forEach { m ->
                        val selected = e.mood == m
                        Surface(
                            onClick = { e = e.copy(mood = m) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (selected) moodColor(m).copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
                            border = if (selected) BorderStroke(2.dp, moodColor(m)) else null,
                        ) {
                            Text(moodEmoji(m), fontSize = 32.sp, modifier = Modifier.padding(8.dp))
                        }
                    }
                }
                Text(
                    moodLabel(e.mood),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    color = moodColor(e.mood),
                )
            }

            if (e.mood == 1 || CRISIS_SYMPTOM in e.symptoms) CrisisCard(compact = true)

            SectionCard("Samopoczucie") {
                LabeledSlider("⚡ Energia", energyLabel(e.energy), e.energy.toFloat(), 1f..5f, 3) {
                    e = e.copy(energy = it.roundToInt())
                }
                LabeledSlider("😰 Poziom lęku / napięcia", "${e.anxiety} / 10", e.anxiety.toFloat(), 0f..10f, 9) {
                    e = e.copy(anxiety = it.roundToInt())
                }
                LabeledSlider("😴 Sen ostatniej nocy", "${e.sleepHours} h", e.sleepHours, 0f..14f, 27) {
                    e = e.copy(sleepHours = (it * 2).roundToInt() / 2f)
                }
            }

            SectionCard("Objawy i dolegliwości") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    data.allSymptoms.forEach { s ->
                        val on = s in e.symptoms
                        FilterChip(
                            selected = on,
                            onClick = { e = e.copy(symptoms = if (on) e.symptoms - s else e.symptoms + s) },
                            label = { Text(s) },
                        )
                    }
                    AssistChip(
                        onClick = { addingSymptom = true },
                        label = { Text("Własny") },
                        leadingIcon = { Icon(Icons.Default.Add, null) },
                    )
                }
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("💊 Leki przyjęte zgodnie z planem", Modifier.weight(1f))
                    Switch(checked = e.medsTaken, onCheckedChange = { e = e.copy(medsTaken = it) })
                }
            }

            SectionCard("Notatka") {
                OutlinedTextField(
                    value = e.note,
                    onValueChange = { e = e.copy(note = it) },
                    placeholder = { Text("Co się dziś wydarzyło? Jakie myśli i emocje się pojawiły?") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = e.helped,
                    onValueChange = { e = e.copy(helped = it) },
                    label = { Text("Co pomogło, a co pogorszyło samopoczucie?") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (exists) {
                OutlinedButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Usuń wpis", color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (pickingDate) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = e.localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) =
                    utcTimeMillis <= LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            },
        )
        DatePickerDialog(
            onDismissRequest = { pickingDate = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { ms ->
                        val d = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate().toString()
                        // Switching to a day that already has an entry opens that entry.
                        e = data.entries[d] ?: e.copy(date = d)
                    }
                    pickingDate = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { pickingDate = false }) { Text("Anuluj") } },
        ) { DatePicker(state = state) }
    }

    if (addingSymptom) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { addingSymptom = false },
            title = { Text("Własny objaw") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(40) },
                    label = { Text("Nazwa, np. Zawroty głowy") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(enabled = name.isNotBlank(), onClick = {
                    val s = name.trim()
                    onAddSymptom(s)
                    e = e.copy(symptoms = e.symptoms + s)
                    addingSymptom = false
                }) { Text("Dodaj") }
            },
            dismissButton = { TextButton(onClick = { addingSymptom = false }) { Text("Anuluj") } },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Usunąć wpis?") },
            text = { Text("Wpis z dnia ${longDate(e.localDate)} zostanie trwale usunięty.") },
            confirmButton = {
                TextButton(onClick = { onDelete(e.date) }) { Text("Usuń", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Anuluj") } },
        )
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: String,
    current: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onChange: (Float) -> Unit,
) {
    Column(Modifier.padding(bottom = 4.dp)) {
        Row {
            Text(label, Modifier.weight(1f))
            Text(value, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = current, onValueChange = onChange, valueRange = range, steps = steps)
    }
}

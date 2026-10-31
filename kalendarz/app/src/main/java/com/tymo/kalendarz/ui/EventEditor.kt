@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.tymo.kalendarz.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.tymo.kalendarz.Event
import com.tymo.kalendarz.Repeat
import com.tymo.kalendarz.calColors
import com.tymo.kalendarz.reminderChoices
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@Composable
fun EventEditor(initial: Event, onClose: () -> Unit, onSave: (Event) -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initial.title) }
    var allDay by remember { mutableStateOf(initial.allDay) }
    var start by remember { mutableStateOf(initial.start) }
    var end by remember { mutableStateOf(initial.end) }
    var color by remember { mutableStateOf(initial.color) }
    var repeat by remember { mutableStateOf(initial.repeat) }
    var reminder by remember { mutableStateOf(initial.reminder) }
    var location by remember { mutableStateOf(initial.location) }
    var description by remember { mutableStateOf(initial.description) }
    var picker by remember { mutableStateOf<String?>(null) } // sd, st, ed, et
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    BackHandler(onBack = onClose)

    // Moving the start keeps the event length, like most calendars do.
    fun setStart(s: LocalDateTime) {
        val len = ChronoUnit.MINUTES.between(start, end).coerceAtLeast(0)
        start = s
        end = s.plusMinutes(len)
    }
    fun setEnd(e: LocalDateTime) {
        end = if (e.isBefore(start)) start.plusMinutes(if (allDay) 0 else 60) else e
    }

    fun save() {
        val (s, e) = if (allDay) start.toLocalDate().atStartOfDay() to end.toLocalDate().atTime(23, 59) else start to end
        onSave(initial.copy(title = title.trim().ifBlank { "(bez tytułu)" }, allDay = allDay, start = s, end = e, color = color, repeat = repeat, reminder = reminder, location = location.trim(), description = description.trim()))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Zamknij") } },
                title = {},
                actions = { Button(onClick = { save() }, modifier = Modifier.padding(end = 8.dp)) { Text("Zapisz") } },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().imePadding().verticalScroll(rememberScrollState())) {
            TextField(
                value = title,
                onValueChange = { title = it.take(100) },
                placeholder = { Text("Dodaj tytuł", fontSize = 24.sp) },
                textStyle = TextStyle(fontSize = 24.sp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth().padding(start = 40.dp, end = 12.dp),
            )
            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            RowItem("🕒") {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Cały dzień", Modifier.weight(1f))
                    Switch(checked = allDay, onCheckedChange = { allDay = it })
                }
            }
            RowItem("") { DateTimeRow(start, allDay, onDate = { picker = "sd" }, onTime = { picker = "st" }) }
            RowItem("") { DateTimeRow(end, allDay, onDate = { picker = "ed" }, onTime = { picker = "et" }) }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            RowItem("🔁") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Repeat.entries.forEach { r -> FilterChip(selected = repeat == r, onClick = { repeat = r }, label = { Text(r.label) }) }
                }
            }
            RowItem("🔔") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    reminderChoices.forEach { (m, l) ->
                        FilterChip(selected = reminder == m, onClick = {
                            reminder = m
                            if (m != null && Build.VERSION.SDK_INT >= 33 &&
                                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                            ) permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }, label = { Text(l) })
                    }
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            RowItem("🎨") {
                Column {
                    Text(calColors[color].name, style = MaterialTheme.typography.bodyMedium)
                    FlowRow(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        calColors.indices.forEach { i ->
                            Box(
                                Modifier.size(30.dp).background(eventColor(i), CircleShape)
                                    .then(if (i == color) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                                    .clickable { color = i },
                                contentAlignment = Alignment.Center,
                            ) { if (i == color) Text("✓", color = Color.White, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            RowItem("📍") {
                OutlinedTextField(location, { location = it.take(100) }, placeholder = { Text("Dodaj lokalizację") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            RowItem("📝") {
                OutlinedTextField(description, { description = it }, placeholder = { Text("Dodaj opis") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    when (picker) {
        "sd" -> DatePick(start.toLocalDate(), { picker = null }) { setStart(it.atTime(start.toLocalTime())) }
        "st" -> TimePick(start.toLocalTime(), { picker = null }) { setStart(start.toLocalDate().atTime(it)) }
        "ed" -> DatePick(end.toLocalDate(), { picker = null }) { setEnd(it.atTime(end.toLocalTime())) }
        "et" -> TimePick(end.toLocalTime(), { picker = null }) { setEnd(end.toLocalDate().atTime(it)) }
    }
}

@Composable
private fun RowItem(icon: String, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) { Text(icon, fontSize = 18.sp) }
        Box(Modifier.weight(1f).padding(start = 12.dp)) { content() }
    }
}

@Composable
private fun DateTimeRow(t: LocalDateTime, allDay: Boolean, onDate: () -> Unit, onTime: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onDate, modifier = Modifier.weight(1f)) {
            Text(dateMedium(t.toLocalDate()), Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurface)
        }
        if (!allDay) TextButton(onClick = onTime) { Text(t.format(HM), color = MaterialTheme.colorScheme.onSurface) }
    }
}

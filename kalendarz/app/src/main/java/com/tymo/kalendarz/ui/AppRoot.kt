@file:OptIn(ExperimentalMaterial3Api::class)

package com.tymo.kalendarz.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tymo.kalendarz.AppViewModel
import com.tymo.kalendarz.DEFAULT_COLOR
import com.tymo.kalendarz.Event
import com.tymo.kalendarz.Export
import com.tymo.kalendarz.Occurrence
import com.tymo.kalendarz.weekStart
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import kotlin.math.abs

enum class View(val label: String) { MONTH("Miesiąc"), WEEK("Tydzień"), DAY("Dzień"), AGENDA("Lista") }

@Composable
fun AppRoot(vm: AppViewModel = viewModel()) {
    val data by vm.data.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var view by rememberSaveable { mutableIntStateOf(0) }
    var anchorText by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val anchor = LocalDate.parse(anchorText)
    fun setAnchor(d: LocalDate) { anchorText = d.toString() }
    var editing by remember { mutableStateOf<Event?>(null) }
    var showing by remember { mutableStateOf<Occurrence?>(null) }
    var menu by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }
    val current = View.entries[view]

    fun step(dir: Int) = setAnchor(
        when (current) {
            View.MONTH -> anchor.plusMonths(dir.toLong()).withDayOfMonth(1)
            View.WEEK -> anchor.plusWeeks(dir.toLong())
            View.DAY -> anchor.plusDays(dir.toLong())
            View.AGENDA -> anchor.plusMonths(dir.toLong())
        }
    )

    fun newEvent(date: LocalDate, hour: Int? = null) {
        val h = hour ?: (LocalTime.now().hour + 1).coerceAtMost(23)
        val start = date.atTime(h, 0)
        editing = Event(0, "", start, start.plusHours(1), color = DEFAULT_COLOR)
    }

    val title = when (current) {
        View.MONTH, View.AGENDA -> Export.monthTitle(YearMonth.from(anchor))
        View.WEEK -> {
            val s = weekStart(anchor)
            val e = s.plusDays(6)
            if (s.month == e.month) Export.monthTitle(YearMonth.from(s))
            else cap(s.format(java.time.format.DateTimeFormatter.ofPattern("LLL", com.tymo.kalendarz.PL))) + " – " +
                e.format(java.time.format.DateTimeFormatter.ofPattern("LLL yyyy", com.tymo.kalendarz.PL))
        }
        View.DAY -> dateLong(anchor)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1) },
                    actions = {
                        IconButton(onClick = { step(-1) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Wstecz") }
                        IconButton(onClick = { step(1) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Dalej") }
                        Surface(
                            onClick = { setAnchor(LocalDate.now()) },
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.onSurfaceVariant),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(28.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("${LocalDate.now().dayOfMonth}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, "Więcej") }
                        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                            DropdownMenuItem(text = { Text("📤 Eksportuj miesiąc / tydzień") }, onClick = { menu = false; exporting = true })
                            DropdownMenuItem(
                                text = { Text("Pokazuj polskie święta") },
                                leadingIcon = { Checkbox(checked = data.settings.holidays, onCheckedChange = null) },
                                onClick = { vm.setHolidays(!data.settings.holidays) },
                            )
                        }
                    },
                )
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                    View.entries.forEachIndexed { i, v ->
                        SegmentedButton(
                            selected = view == i,
                            onClick = { view = i },
                            shape = SegmentedButtonDefaults.itemShape(i, View.entries.size),
                            icon = {},
                        ) { Text(v.label, fontSize = 13.sp, maxLines = 1) }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { newEvent(anchor) }) { Icon(Icons.Default.Add, "Nowe wydarzenie") }
        },
    ) { padding ->
        var drag by remember { mutableFloatStateOf(0f) }
        Box(
            Modifier.padding(padding).fillMaxSize().pointerInput(current, anchorText) {
                detectHorizontalDragGestures(
                    onDragStart = { drag = 0f },
                    onDragEnd = { if (abs(drag) > 120f) step(if (drag < 0) 1 else -1) },
                ) { _, dx -> drag += dx }
            },
        ) {
            when (current) {
                View.MONTH -> MonthView(data, YearMonth.from(anchor), onDay = { setAnchor(it); view = View.DAY.ordinal }, onOpen = { showing = it })
                View.WEEK -> TimeGrid(data, (0..6).map { weekStart(anchor).plusDays(it.toLong()) }, onOpen = { showing = it }, onEmpty = { d, h -> newEvent(d, h) }, onDay = { setAnchor(it); view = View.DAY.ordinal })
                View.DAY -> TimeGrid(data, listOf(anchor), onOpen = { showing = it }, onEmpty = { d, h -> newEvent(d, h) }, onDay = {})
                View.AGENDA -> AgendaView(data, anchor, onOpen = { showing = it })
            }
        }
    }

    showing?.let { o ->
        EventSheet(
            o = o,
            onDismiss = { showing = null },
            onEdit = { showing = null; editing = o.event },
            onDelete = { showing = null; vm.deleteEvent(o.event.id) },
        )
    }

    editing?.let { e ->
        EventEditor(
            initial = e,
            onClose = { editing = null },
            onSave = { vm.saveEvent(it); editing = null; setAnchor(it.start.toLocalDate()) },
        )
    }

    if (exporting) {
        ExportDialog(anchor, onDismiss = { exporting = false }) { week, pdf ->
            exporting = false
            runCatching {
                if (week) {
                    val s = weekStart(anchor)
                    val file = if (pdf) Export.weekPdf(context, data, s) else Export.ics(context, data, s, s.plusDays(6), "Kalendarz tydzien $s")
                    Export.share(context, file, if (pdf) "application/pdf" else "text/calendar", Export.weekTitle(s))
                } else {
                    val m = YearMonth.from(anchor)
                    val file = if (pdf) Export.monthPdf(context, data, m) else Export.ics(context, data, m.atDay(1), m.atEndOfMonth(), "Kalendarz $m")
                    Export.share(context, file, if (pdf) "application/pdf" else "text/calendar", Export.monthTitle(m))
                }
            }.onFailure { Toast.makeText(context, "Eksport nie powiódł się", Toast.LENGTH_SHORT).show() }
        }
    }
}

@Composable
private fun ExportDialog(anchor: LocalDate, onDismiss: () -> Unit, onExport: (week: Boolean, pdf: Boolean) -> Unit) {
    var week by remember { mutableStateOf(false) }
    var pdf by remember { mutableStateOf(true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eksport") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Zakres", style = MaterialTheme.typography.labelLarge)
                Choice(!week, "Miesiąc: ${Export.monthTitle(YearMonth.from(anchor))}") { week = false }
                Choice(week, Export.weekTitle(weekStart(anchor))) { week = true }
                Text("Format", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                Choice(pdf, "PDF – do wydruku lub wysłania") { pdf = true }
                Choice(!pdf, "ICS – do importu w Google Calendar, Outlook, iPhone") { pdf = false }
            }
        },
        confirmButton = { Button(onClick = { onExport(week, pdf) }) { Text("Eksportuj") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } },
    )
}

@Composable
private fun Choice(selected: Boolean, label: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

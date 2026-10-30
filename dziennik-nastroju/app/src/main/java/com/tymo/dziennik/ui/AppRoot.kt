@file:OptIn(ExperimentalMaterial3Api::class)

package com.tymo.dziennik.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tymo.dziennik.Entry
import com.tymo.dziennik.JournalViewModel
import com.tymo.dziennik.report
import java.time.LocalDate

private enum class Tab(val label: String, val icon: ImageVector) {
    JOURNAL("Dziennik", Icons.Default.Home),
    STATS("Statystyki", Icons.Default.DateRange),
    HELP("Wsparcie", Icons.Default.Favorite),
}

@Composable
fun AppRoot(vm: JournalViewModel = viewModel()) {
    val data by vm.data.collectAsStateWithLifecycle()
    val unlocked by vm.unlocked.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var editing by remember { mutableStateOf<Entry?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var newPin by remember { mutableStateOf<String?>(null) }
    var settingPin by remember { mutableStateOf(false) }

    if (!unlocked) {
        PinPad("Wpisz PIN", onPin = vm::tryUnlock)
        return
    }

    if (settingPin) {
        val first = newPin
        if (first == null) {
            PinPad("Ustaw nowy PIN", onPin = { newPin = it; true }, onCancel = { settingPin = false })
        } else {
            PinPad("Powtórz PIN", onPin = { p ->
                if (p == first) {
                    vm.setPin(p)
                    settingPin = false
                    newPin = null
                    true
                } else {
                    newPin = null
                    false
                }
            }, onCancel = { settingPin = false; newPin = null })
        }
        return
    }

    editing?.let { e ->
        EntryEditor(
            initial = e,
            data = data,
            onClose = { editing = null },
            onSave = { vm.saveEntry(it); editing = null },
            onDelete = { vm.deleteEntry(it); editing = null },
            onAddSymptom = vm::addCustomSymptom,
        )
        return
    }

    val current = Tab.entries[tab]
    val today = LocalDate.now().toString()
    val todayEntry = data.entries[today]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (current == Tab.JOURNAL) "Mój Dziennik" else current.label, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Ustawienia")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEachIndexed { i, t ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { Icon(t.icon, null) },
                        label = { Text(t.label) },
                    )
                }
            }
        },
        floatingActionButton = {
            if (current == Tab.JOURNAL) {
                ExtendedFloatingActionButton(
                    onClick = { editing = todayEntry ?: Entry(date = today) },
                    icon = { Icon(Icons.Default.Edit, null) },
                    text = { Text(if (todayEntry == null) "Dzisiejszy wpis" else "Edytuj dzisiejszy") },
                )
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (current) {
                Tab.JOURNAL -> JournalScreen(data, onOpen = { editing = it })
                Tab.STATS -> StatsScreen(data)
                Tab.HELP -> HelpScreen()
            }
        }
    }

    if (showSettings) {
        SettingsDialog(
            hasPin = data.prefs.pinHash != null,
            reminder = data.prefs.reminder,
            reminderHour = data.prefs.reminderHour,
            report = { data.report(30) },
            onDismiss = { showSettings = false },
            onSetPin = { showSettings = false; settingPin = true },
            onRemovePin = { vm.setPin(null) },
            onReminder = vm::setReminder,
        )
    }
}

@Composable
private fun SettingsDialog(
    hasPin: Boolean,
    reminder: Boolean,
    reminderHour: Int,
    report: () -> String,
    onDismiss: () -> Unit,
    onSetPin: () -> Unit,
    onRemovePin: () -> Unit,
    onReminder: (Boolean, Int) -> Unit,
) {
    val context = LocalContext.current
    var hour by remember { mutableStateOf(reminderHour.toFloat()) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        onReminder(granted, hour.toInt())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ustawienia") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("🔒 Blokada PIN-em", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onSetPin) { Text(if (hasPin) "Zmień PIN" else "Ustaw PIN") }
                    if (hasPin) OutlinedButton(onClick = onRemovePin) { Text("Wyłącz") }
                }
                HorizontalDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("🔔 Codzienne przypomnienie", fontWeight = FontWeight.SemiBold)
                        Text("Godzina: ${hour.toInt()}:00", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = reminder, onCheckedChange = { on ->
                        if (on && Build.VERSION.SDK_INT >= 33) {
                            permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onReminder(on, hour.toInt())
                        }
                    })
                }
                Slider(
                    value = hour,
                    onValueChange = { hour = it },
                    onValueChangeFinished = { if (reminder) onReminder(true, hour.toInt()) },
                    valueRange = 6f..23f,
                    steps = 16,
                )
                HorizontalDivider()
                Text("📤 Raport dla lekarza / terapeuty", fontWeight = FontWeight.SemiBold)
                OutlinedButton(onClick = {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Mój Dziennik – raport")
                        putExtra(Intent.EXTRA_TEXT, report())
                    }
                    context.startActivity(Intent.createChooser(send, "Udostępnij raport"))
                }, modifier = Modifier.fillMaxWidth()) { Text("Udostępnij ostatnie 30 dni") }
                Text(
                    "Dane są zapisane tylko na tym telefonie. Aplikacja nie zastępuje porady lekarza.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Zamknij") } },
    )
}

@file:OptIn(ExperimentalMaterial3Api::class)

package com.tymo.zdrowydzien.ui

import android.Manifest
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tymo.zdrowydzien.AppViewModel
import com.tymo.zdrowydzien.Settings

private enum class Tab(val label: String, val icon: ImageVector) {
    TODAY("Dziś", Icons.Default.Home),
    HABITS("Nawyki", Icons.Default.CheckCircle),
    MEALS("Posiłki", Icons.Default.Favorite),
    HISTORY("Historia", Icons.Default.DateRange),
}

@Composable
fun AppScreen(vm: AppViewModel = viewModel()) {
    val data by vm.data.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }
    val current = Tab.entries[tab]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (current == Tab.TODAY) "Zdrowy Dzień" else current.label,
                        fontWeight = FontWeight.Bold,
                    )
                },
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
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (current) {
                Tab.TODAY -> TodayScreen(data, vm, onOpenMeals = { tab = Tab.MEALS.ordinal })
                Tab.HABITS -> HabitsScreen(data, vm)
                Tab.MEALS -> MealsScreen(data, vm)
                Tab.HISTORY -> HistoryScreen(data)
            }
        }
    }

    if (showSettings) {
        SettingsDialog(
            initial = data.settings,
            onDismiss = { showSettings = false },
            onSave = {
                vm.saveSettings(it)
                showSettings = false
            },
        )
    }
}

@Composable
private fun SettingsDialog(initial: Settings, onDismiss: () -> Unit, onSave: (Settings) -> Unit) {
    var kcal by remember { mutableStateOf(initial.kcalGoal.toString()) }
    var water by remember { mutableStateOf(initial.waterGoal.toString()) }
    var glass by remember { mutableStateOf(initial.glassMl.toString()) }
    var reminders by remember { mutableStateOf(initial.reminders) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        reminders = granted
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ustawienia") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField("Cel kalorii (kcal)", kcal) { kcal = it }
                NumberField("Cel wody (ml)", water) { water = it }
                NumberField("Wielkość szklanki (ml)", glass) { glass = it }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Przypomnienia o wodzie")
                        Text(
                            "Co ok. 2 godziny, 8:00–22:00",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = reminders,
                        onCheckedChange = { on ->
                            if (on && Build.VERSION.SDK_INT >= 33) {
                                permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                reminders = on
                            }
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    Settings(
                        kcalGoal = kcal.toIntOrNull()?.coerceIn(500, 10000) ?: initial.kcalGoal,
                        waterGoal = water.toIntOrNull()?.coerceIn(250, 10000) ?: initial.waterGoal,
                        glassMl = glass.toIntOrNull()?.coerceIn(50, 2000) ?: initial.glassMl,
                        reminders = reminders,
                    )
                )
            }) { Text("Zapisz") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } },
    )
}

@Composable
fun NumberField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { v -> onChange(v.filter { it.isDigit() }.take(5)) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
    )
}

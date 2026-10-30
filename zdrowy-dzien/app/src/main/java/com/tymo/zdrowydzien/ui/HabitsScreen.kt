@file:OptIn(ExperimentalLayoutApi::class)

package com.tymo.zdrowydzien.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tymo.zdrowydzien.AppData
import com.tymo.zdrowydzien.AppViewModel
import com.tymo.zdrowydzien.Habit

private val emojiChoices = listOf("✅", "🚶", "🏃", "🧘", "📚", "💤", "😴", "🍬", "🥦", "💊", "🦷", "🧹", "✍️", "🎸", "🚭", "📵")

@Composable
fun HabitsScreen(data: AppData, vm: AppViewModel) {
    var adding by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Habit?>(null) }
    val days = lastDays(7)

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    "Dotknij kółka, aby odhaczyć dzień (także wstecz).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (data.habits.isEmpty()) {
                item { Text("Brak nawyków. Dodaj pierwszy przyciskiem poniżej.", Modifier.padding(top = 24.dp)) }
            }
            items(data.habits, key = { it.id }) { h ->
                Card(Modifier.fillMaxWidth().animateItem()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(h.emoji, style = MaterialTheme.typography.headlineSmall)
                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(h.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                val streak = data.streak(h.id)
                                val week = days.count { data.isDone(h.id, it.toString()) }
                                Text(
                                    (if (streak > 0) "🔥 Seria: $streak dni · " else "") + "W tym tygodniu: $week/7",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(onClick = { toDelete = h }) {
                                Icon(Icons.Default.Delete, "Usuń", tint = MaterialTheme.colorScheme.outline)
                            }
                        }
                        Row(
                            Modifier.fillMaxWidth().padding(top = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            days.forEach { day ->
                                val done = data.isDone(h.id, day.toString())
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(shortDay(day), style = MaterialTheme.typography.labelSmall)
                                    DayDot(done) { vm.toggleHabit(h.id, day.toString()) }
                                }
                            }
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { adding = true },
            icon = { Icon(Icons.Default.Add, null) },
            text = { Text("Nowy nawyk") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }

    if (adding) {
        AddHabitDialog(onDismiss = { adding = false }) { name, emoji ->
            vm.addHabit(name, emoji)
            adding = false
        }
    }

    toDelete?.let { h ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Usunąć nawyk?") },
            text = { Text("„${h.name}” i jego historia zostaną usunięte.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteHabit(h.id)
                    toDelete = null
                }) { Text("Usuń", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Anuluj") } },
        )
    }
}

@Composable
private fun DayDot(done: Boolean, onClick: () -> Unit) {
    val base = Modifier.padding(top = 4.dp).size(34.dp).clickable(onClick = onClick)
    Box(
        if (done) base.background(HabitGreen, CircleShape)
        else base.border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (done) Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AddHabitDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("✅") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nowy nawyk") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa, np. Medytacja 10 min") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Ikona", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    emojiChoices.forEach { e ->
                        FilterChip(selected = emoji == e, onClick = { emoji = e }, label = { Text(e) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = { onAdd(name.trim(), emoji) }) { Text("Dodaj") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } },
    )
}

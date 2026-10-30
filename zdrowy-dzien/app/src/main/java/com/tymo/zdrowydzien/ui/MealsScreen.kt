@file:OptIn(ExperimentalLayoutApi::class)

package com.tymo.zdrowydzien.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tymo.zdrowydzien.AppData
import com.tymo.zdrowydzien.AppViewModel
import com.tymo.zdrowydzien.MealType
import com.tymo.zdrowydzien.quickFoods
import java.time.LocalDate
import java.time.LocalTime

private fun defaultMealType(): MealType = when (LocalTime.now().hour) {
    in 5..10 -> MealType.BREAKFAST
    in 11..15 -> MealType.LUNCH
    in 16..20 -> MealType.DINNER
    else -> MealType.SNACK
}

@Composable
fun MealsScreen(data: AppData, vm: AppViewModel) {
    var adding by remember { mutableStateOf(false) }
    val d = LocalDate.now().toString()
    val meals = data.mealsOn(d)
    val total = meals.sumOf { it.kcal }
    val goal = data.settings.kcalGoal

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                SectionCard("Dzisiaj: $total / $goal kcal") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LinearProgressIndicator(
                            progress = { (total.toFloat() / goal).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (total > goal) MaterialTheme.colorScheme.error else KcalOrange,
                            drawStopIndicator = {},
                        )
                        MealType.entries.forEach { t ->
                            val sum = meals.filter { it.type == t }.sumOf { it.kcal }
                            Row {
                                Text("${t.emoji} ${t.label}", Modifier.weight(1f))
                                Text("$sum kcal", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
            if (meals.isEmpty()) {
                item { Text("Nie dodano dziś posiłków.", Modifier.padding(top = 16.dp)) }
            }
            items(meals.sortedByDescending { it.id }, key = { it.id }) { m ->
                Card(Modifier.fillMaxWidth().animateItem()) {
                    Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(m.type.emoji, style = MaterialTheme.typography.titleLarge)
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(m.name, style = MaterialTheme.typography.titleMedium)
                            Text(m.type.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${m.kcal} kcal", fontWeight = FontWeight.SemiBold, color = KcalOrange)
                        IconButton(onClick = { vm.deleteMeal(m.id) }) {
                            Icon(Icons.Default.Delete, "Usuń", tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { adding = true },
            icon = { Icon(Icons.Default.Add, null) },
            text = { Text("Dodaj posiłek") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }

    if (adding) {
        AddMealDialog(onDismiss = { adding = false }) { name, kcal, type ->
            vm.addMeal(name, kcal, type)
            adding = false
        }
    }
}

@Composable
private fun AddMealDialog(onDismiss: () -> Unit, onAdd: (String, Int, MealType) -> Unit) {
    var name by remember { mutableStateOf("") }
    var kcal by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(defaultMealType()) }
    val kcalValue = kcal.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj posiłek") },
        text = {
            Column(
                Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Co zjadłeś?") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                NumberField("Kalorie (kcal)", kcal) { kcal = it }
                Text("Rodzaj", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MealType.entries.forEach { t ->
                        FilterChip(selected = type == t, onClick = { type = t }, label = { Text("${t.emoji} ${t.label}") })
                    }
                }
                Text("Szybkie dodawanie", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    quickFoods.forEach { (food, cal) ->
                        AssistChip(
                            onClick = {
                                name = food
                                kcal = cal.toString()
                            },
                            label = { Text("$food · $cal") },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && kcalValue != null && kcalValue > 0,
                onClick = { onAdd(name.trim(), kcalValue ?: 0, type) },
            ) { Text("Dodaj") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } },
    )
}

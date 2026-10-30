package com.tymo.zdrowydzien.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tymo.zdrowydzien.AppData

@Composable
fun HistoryScreen(data: AppData) {
    val days = lastDays(7)
    val labels = days.map { shortDay(it) }
    val keys = days.map { it.toString() }
    val water = keys.map { data.waterOn(it).toFloat() }
    val kcal = keys.map { data.kcalOn(it).toFloat() }
    val habits = keys.map {
        if (data.habits.isEmpty()) 0f else data.habitsDoneOn(it) * 100f / data.habits.size
    }
    val s = data.settings
    val waterDaysOk = water.count { it >= s.waterGoal }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Ostatnie 7 dni", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

        SectionCard("💧 Woda (ml)") {
            Column {
                BarChart(water, labels, WaterBlue, goal = s.waterGoal.toFloat())
                Stat("Średnio", "${water.average().toInt()} ml / dzień")
                Stat("Cel osiągnięty", "$waterDaysOk z 7 dni")
            }
        }

        SectionCard("🍽️ Kalorie (kcal)") {
            Column {
                BarChart(kcal, labels, KcalOrange, goal = s.kcalGoal.toFloat())
                val logged = kcal.filter { it > 0 }
                Stat("Średnio (dni z wpisami)", if (logged.isEmpty()) "–" else "${logged.average().toInt()} kcal")
            }
        }

        SectionCard("✅ Nawyki (% wykonanych)") {
            Column {
                BarChart(habits, labels, HabitGreen, goal = 100f)
                Stat("Średnio", "${habits.average().toInt()}%")
                val best = data.habits.maxByOrNull { data.streak(it.id) }
                if (best != null && data.streak(best.id) > 0) {
                    Stat("Najdłuższa seria", "${best.emoji} ${best.name}: ${data.streak(best.id)} dni")
                }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text(label, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

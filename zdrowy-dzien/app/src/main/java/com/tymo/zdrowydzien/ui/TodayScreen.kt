package com.tymo.zdrowydzien.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tymo.zdrowydzien.AppData
import com.tymo.zdrowydzien.AppViewModel
import java.time.LocalDate

@Composable
fun TodayScreen(data: AppData, vm: AppViewModel, onOpenMeals: () -> Unit) {
    val today = LocalDate.now()
    val d = today.toString()
    val s = data.settings
    val water = data.waterOn(d)
    val kcal = data.kcalOn(d)
    val habitsDone = data.habitsDoneOn(d)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(longDate(today), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(
            motivation(water >= s.waterGoal, data.habits.isNotEmpty() && habitsDone == data.habits.size),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Summary rings
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            SummaryRing("Woda", water.toFloat() / s.waterGoal, WaterBlue, "${water}", "/ ${s.waterGoal} ml")
            SummaryRing("Kalorie", kcal.toFloat() / s.kcalGoal, KcalOrange, "$kcal", "/ ${s.kcalGoal}")
            SummaryRing(
                "Nawyki",
                if (data.habits.isEmpty()) 0f else habitsDone.toFloat() / data.habits.size,
                HabitGreen, "$habitsDone", "/ ${data.habits.size}",
            )
        }

        SectionCard("💧 Woda") {
            Column {
                val left = (s.waterGoal - water).coerceAtLeast(0)
                Text(
                    if (left == 0) "Cel osiągnięty! Świetnie 💪"
                    else "Zostało $left ml (ok. ${(left + s.glassMl - 1) / s.glassMl} szklanek)",
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { vm.addWater(s.glassMl) }, Modifier.weight(1f)) {
                        Text("+ ${s.glassMl} ml")
                    }
                    FilledTonalButton(onClick = { vm.addWater(500) }, Modifier.weight(1f)) {
                        Text("+ 500 ml")
                    }
                    OutlinedButton(onClick = { vm.addWater(-s.glassMl) }, enabled = water > 0) {
                        Text("−")
                    }
                }
            }
        }

        SectionCard("✅ Nawyki na dziś") {
            Column {
                if (data.habits.isEmpty()) {
                    Text("Dodaj nawyki w zakładce Nawyki.")
                }
                data.habits.forEach { h ->
                    val done = data.isDone(h.id, d)
                    Row(
                        Modifier.fillMaxWidth().clickable { vm.toggleHabit(h.id) },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = done, onCheckedChange = { vm.toggleHabit(h.id) })
                        Text("${h.emoji}  ${h.name}", Modifier.weight(1f))
                        val streak = data.streak(h.id)
                        if (streak > 0) Text("🔥 $streak", color = KcalOrange, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        SectionCard("🍽️ Kalorie") {
            Column {
                val left = s.kcalGoal - kcal
                Text(
                    if (left >= 0) "Zjedzono $kcal kcal, zostało $left kcal"
                    else "Przekroczono cel o ${-left} kcal",
                )
                Spacer(Modifier.height(12.dp))
                FilledTonalButton(onClick = onOpenMeals, Modifier.fillMaxWidth()) {
                    Text("Dodaj posiłek")
                }
            }
        }
    }
}

@Composable
private fun SummaryRing(label: String, progress: Float, color: androidx.compose.ui.graphics.Color, value: String, sub: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ProgressRing(progress, color, size = 96.dp, stroke = 10.dp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.width(4.dp).height(6.dp))
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

private fun motivation(waterDone: Boolean, habitsDone: Boolean) = when {
    waterDone && habitsDone -> "Perfekcyjny dzień! Wszystkie cele zrealizowane 🎉"
    habitsDone -> "Nawyki odhaczone! Pamiętaj jeszcze o wodzie 💧"
    waterDone -> "Woda wypita! Czas na nawyki ✅"
    else -> "Małe kroki każdego dnia robią różnicę."
}

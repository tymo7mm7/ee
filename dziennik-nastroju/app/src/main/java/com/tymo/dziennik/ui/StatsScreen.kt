package com.tymo.dziennik.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tymo.dziennik.JournalData
import com.tymo.dziennik.moodEmoji
import java.time.LocalDate

private fun fmt(v: Double) = "%.1f".format(v)

@Composable
fun StatsScreen(data: JournalData) {
    var days by rememberSaveable { mutableIntStateOf(30) }
    val today = LocalDate.now()
    val range = (days - 1 downTo 0).map { today.minusDays(it.toLong()) }
    val inRange = range.mapNotNull { data.entries[it.toString()] }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(7, 30, 90).forEach { d ->
                FilterChip(selected = days == d, onClick = { days = d }, label = { Text("$d dni") })
            }
        }

        if (inRange.isEmpty()) {
            Text("Brak wpisów w tym okresie. Statystyki pojawią się po kilku dniach prowadzenia dziennika.")
            return@Column
        }

        val avgMood = inRange.map { it.mood }.average()
        SectionCard("Podsumowanie") {
            Stat("Wpisy", "${inRange.size} z $days dni")
            Stat("Średni nastrój", "${moodEmoji(avgMood.toInt().coerceIn(1, 5))} ${fmt(avgMood)} / 5")
            Stat("Średnia energia", "${fmt(inRange.map { it.energy }.average())} / 5")
            Stat("Średni lęk", "${fmt(inRange.map { it.anxiety }.average())} / 10")
            Stat("Średni sen", "${fmt(inRange.map { it.sleepHours.toDouble() }.average())} h")
            Stat("Leki przyjęte", "${inRange.count { it.medsTaken }} dni")
        }

        SectionCard("Nastrój w czasie") {
            LineChart(range.map { d -> data.entries[d.toString()]?.mood?.toFloat() }, 1f, 5f)
            Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                Text(shortDate(range.first()), Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
                Text(shortDate(range.last()), style = MaterialTheme.typography.labelSmall)
            }
        }

        SectionCard("Lęk / napięcie w czasie") {
            LineChart(range.map { d -> data.entries[d.toString()]?.anxiety?.toFloat() }, 0f, 10f)
        }

        val freq = inRange.flatMap { it.symptoms }.groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }.take(8)
        if (freq.isNotEmpty()) {
            SectionCard("Najczęstsze objawy") {
                freq.forEach { (name, count) ->
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Row {
                            Text(name, Modifier.weight(1f))
                            Text("$count×", fontWeight = FontWeight.SemiBold)
                        }
                        LinearProgressIndicator(
                            progress = { count.toFloat() / inRange.size },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            drawStopIndicator = {},
                        )
                    }
                }
            }
        }

        val good = inRange.filter { it.sleepHours >= 7f }
        val bad = inRange.filter { it.sleepHours < 7f }
        if (good.isNotEmpty() && bad.isNotEmpty()) {
            SectionCard("💡 Sen a nastrój") {
                Stat("Po 7+ h snu", "${fmt(good.map { it.mood }.average())} / 5")
                Stat("Po krótszym śnie", "${fmt(bad.map { it.mood }.average())} / 5")
                val diff = good.map { it.mood }.average() - bad.map { it.mood }.average()
                Text(
                    when {
                        diff > 0.3 -> "Wygląda na to, że dłuższy sen poprawia Twój nastrój."
                        diff < -0.3 -> "W Twoim przypadku dłuższy sen nie wiąże się z lepszym nastrojem."
                        else -> "Nie widać wyraźnego związku między snem a nastrojem."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

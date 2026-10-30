@file:OptIn(ExperimentalLayoutApi::class)

package com.tymo.nagrody.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tymo.nagrody.AppData
import com.tymo.nagrody.formatMinutes
import kotlinx.coroutines.delay

private fun clock(ms: Long): String {
    val s = ms / 1000
    return "%02d:%02d:%02d".format(s / 3600, (s % 3600) / 60, s % 60)
}

@Composable
fun TimerScreen(
    data: AppData,
    onStart: (Long, String) -> Unit,
    onStop: () -> Unit,
    onCancel: () -> Unit,
) {
    val timer = data.timer
    val candidates = data.active.filter { it.needsTime && data.timeProgress(it) < 1f }
    var selected by rememberSaveable { mutableStateOf(candidates.firstOrNull()?.id) }
    var label by rememberSaveable { mutableStateOf("Nauka") }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(timer) {
        while (timer != null) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (timer != null) {
            val reward = data.rewards.find { it.id == timer.rewardId }
            val elapsed = (now - timer.startedAt).coerceAtLeast(0)
            Text("${timer.label} na:", style = MaterialTheme.typography.titleMedium)
            Text("${reward?.emoji ?: ""} ${reward?.name ?: ""}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            ProgressRing(((elapsed / 1000) % 3600) / 3600f, TimeBlue, 240.dp, 16.dp) {
                Text(clock(elapsed), fontSize = 40.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            if (reward != null) {
                val left = (reward.goalMinutes - data.minutes(reward.id) - (elapsed / 60_000).toInt()).coerceAtLeast(0)
                Text(
                    if (left == 0) "Cel czasowy osiągnięty po zapisaniu! 🎉" else "Do celu zostanie: ${formatMinutes(left)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(onClick = onStop, Modifier.fillMaxWidth()) { Text("⏹ Zakończ i zapisz") }
            OutlinedButton(onClick = onCancel, Modifier.fillMaxWidth()) { Text("Anuluj bez zapisu") }
            Text(
                "Stoper działa także po zamknięciu aplikacji.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text("⏱️", fontSize = 64.sp)
            Text(
                "Włącz stoper, gdy zaczynasz naukę lub pracę. Czas doliczy się do wybranej nagrody.",
                textAlign = TextAlign.Center,
            )
            if (candidates.isEmpty()) {
                Text(
                    "Brak nagród z celem czasowym. Dodaj nagrodę typu „Czas” lub „Oba”.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text("Na jaką nagrodę pracujesz?", style = MaterialTheme.typography.labelLarge, modifier = Modifier.fillMaxWidth())
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    candidates.forEach { r ->
                        FilterChip(selected = selected == r.id, onClick = { selected = r.id }, label = { Text("${r.emoji} ${r.name}") })
                    }
                }
                Text("Czym się zajmujesz?", style = MaterialTheme.typography.labelLarge, modifier = Modifier.fillMaxWidth())
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Nauka", "Praca", "Trening", "Obowiązki").forEach { l ->
                        FilterChip(selected = label == l, onClick = { label = l }, label = { Text(l) })
                    }
                }
                Spacer(Modifier.height(8.dp))
                val id = selected?.takeIf { s -> candidates.any { it.id == s } } ?: candidates.first().id
                Button(onClick = { onStart(id, label) }, Modifier.fillMaxWidth()) { Text("▶ Start") }
            }
        }
    }
}

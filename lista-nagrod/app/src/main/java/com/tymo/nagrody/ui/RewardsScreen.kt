package com.tymo.nagrody.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tymo.nagrody.AppData
import com.tymo.nagrody.Kind
import com.tymo.nagrody.Reward
import com.tymo.nagrody.formatMinutes
import com.tymo.nagrody.formatMoney

@Composable
fun RewardsScreen(
    data: AppData,
    onOpen: (Reward) -> Unit,
    onContribute: (Reward, Kind) -> Unit,
    onClaim: (Reward) -> Unit,
) {
    val active = data.active.sortedByDescending { data.progress(it) }
    val totalSaved = data.active.sumOf { data.savedGr(it.id) }
    val totalMinutes = data.contributions.filter { it.kind == Kind.TIME }.sumOf { it.amount }.toInt()

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Stat("💰", formatMoney(totalSaved), "odłożone")
                    Stat("⏱️", formatMinutes(totalMinutes), "nauki i pracy")
                    Stat("🏆", "${data.claimed.size}", "zdobyte")
                }
            }
        }
        if (active.isEmpty()) {
            item {
                Text(
                    "Dodaj nagrodę, na którą chcesz zapracować: książkę, grę, wyjście do kina…",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                )
            }
        }
        items(active, key = { it.id }) { r ->
            RewardCard(r, data, Modifier.animateItem(), onOpen, onContribute, onClaim)
        }
    }
}

@Composable
private fun Stat(icon: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 22.sp)
        Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
private fun RewardCard(
    r: Reward,
    data: AppData,
    modifier: Modifier,
    onOpen: (Reward) -> Unit,
    onContribute: (Reward, Kind) -> Unit,
    onClaim: (Reward) -> Unit,
) {
    val ready = data.isReady(r)
    Card(
        modifier.fillMaxWidth().clickable { onOpen(r) },
        colors = if (ready) CardDefaults.cardColors(containerColor = Gold.copy(alpha = 0.18f)) else CardDefaults.cardColors(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(r.emoji, fontSize = 34.sp)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(r.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (ready) "Gotowe do odebrania! 🎉" else "${(data.progress(r) * 100).toInt()}% ukończone",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (ready) Gold else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (ready) FontWeight.Bold else null,
                    )
                }
            }
            if (r.needsMoney) {
                GoalBar("💰", "${formatMoney(data.savedGr(r.id))} / ${formatMoney(r.priceGr)}", data.moneyProgress(r), MoneyGreen)
            }
            if (r.needsTime) {
                GoalBar("⏱️", "${formatMinutes(data.minutes(r.id))} / ${formatMinutes(r.goalMinutes)}", data.timeProgress(r), TimeBlue)
            }
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (ready) {
                    Button(onClick = { onClaim(r) }, Modifier.weight(1f)) { Text("🎁 Odbierz nagrodę") }
                } else {
                    if (r.needsMoney && data.moneyProgress(r) < 1f) {
                        FilledTonalButton(onClick = { onContribute(r, Kind.MONEY) }, Modifier.weight(1f)) { Text("+ Wpłać") }
                    }
                    if (r.needsTime && data.timeProgress(r) < 1f) {
                        FilledTonalButton(onClick = { onContribute(r, Kind.TIME) }, Modifier.weight(1f)) { Text("+ Czas") }
                    }
                }
            }
        }
    }
}

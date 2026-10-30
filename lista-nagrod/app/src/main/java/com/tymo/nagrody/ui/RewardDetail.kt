@file:OptIn(ExperimentalMaterial3Api::class)

package com.tymo.nagrody.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tymo.nagrody.AppData
import com.tymo.nagrody.Kind
import com.tymo.nagrody.Reward
import com.tymo.nagrody.formatMinutes
import com.tymo.nagrody.formatMoney

@Composable
fun RewardDetail(
    reward: Reward,
    data: AppData,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onContribute: (Kind) -> Unit,
    onDeleteContribution: (Long) -> Unit,
    onClaim: () -> Unit,
    onUnclaim: () -> Unit,
) {
    var confirmDelete by remember { mutableStateOf(false) }
    val ready = data.isReady(reward)
    val claimed = reward.claimedAt != null
    val history = data.history(reward.id)
    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Wróć") }
                },
                title = { Text(reward.name, maxLines = 1) },
                actions = {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edytuj") }
                    IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Default.Delete, "Usuń") }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    ProgressRing(data.progress(reward), if (ready) Gold else MaterialTheme.colorScheme.primary, 180.dp) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(reward.emoji, fontSize = 48.sp)
                            Text("${(data.progress(reward) * 100).toInt()}%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    val missing = buildList {
                        val m = reward.priceGr - data.savedGr(reward.id)
                        if (reward.needsMoney && m > 0) add(formatMoney(m))
                        val t = reward.goalMinutes - data.minutes(reward.id)
                        if (reward.needsTime && t > 0) add(formatMinutes(t))
                    }
                    Text(
                        when {
                            claimed -> "Odebrano ${formatDate(reward.claimedAt!!)} 🏆"
                            missing.isEmpty() -> "Wszystko gotowe – odbierz nagrodę!"
                            else -> "Brakuje: ${missing.joinToString(" i ")}"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        if (reward.needsMoney) {
                            GoalBar("💰", "${formatMoney(data.savedGr(reward.id))} / ${formatMoney(reward.priceGr)}", data.moneyProgress(reward), MoneyGreen)
                        }
                        if (reward.needsTime) {
                            GoalBar("⏱️", "${formatMinutes(data.minutes(reward.id))} / ${formatMinutes(reward.goalMinutes)}", data.timeProgress(reward), TimeBlue)
                        }
                        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            when {
                                claimed -> OutlinedButton(onClick = onUnclaim, Modifier.weight(1f)) { Text("Przywróć do listy") }
                                ready -> Button(onClick = onClaim, Modifier.weight(1f)) { Text("🎁 Odbierz nagrodę") }
                                else -> {
                                    if (reward.needsMoney) FilledTonalButton(onClick = { onContribute(Kind.MONEY) }, Modifier.weight(1f)) { Text("+ Wpłać") }
                                    if (reward.needsTime) FilledTonalButton(onClick = { onContribute(Kind.TIME) }, Modifier.weight(1f)) { Text("+ Czas") }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Text("Historia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
                if (history.isEmpty()) Text("Brak wpisów. Dodaj pierwszą wpłatę lub czas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(history, key = { it.id }) { c ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (c.kind == Kind.MONEY) "💰" else "⏱️", fontSize = 20.sp)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(
                            if (c.kind == Kind.MONEY) "+ ${formatMoney(c.amount)}" else "+ ${formatMinutes(c.amount.toInt())}",
                            fontWeight = FontWeight.SemiBold,
                            color = if (c.kind == Kind.MONEY) MoneyGreen else TimeBlue,
                        )
                        Text(
                            listOf(c.note, formatDateTime(c.at)).filter { it.isNotBlank() }.joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { onDeleteContribution(c.id) }) {
                        Icon(Icons.Default.Close, "Usuń wpis", tint = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Usunąć nagrodę?") },
            text = { Text("„${reward.name}” i cała historia wpłat zostaną usunięte.") },
            confirmButton = { TextButton(onClick = onDelete) { Text("Usuń", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Anuluj") } },
        )
    }
}

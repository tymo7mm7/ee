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
import androidx.compose.material3.Card
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
import com.tymo.nagrody.Reward
import com.tymo.nagrody.formatMinutes
import com.tymo.nagrody.formatMoney

@Composable
fun ClaimedScreen(data: AppData, onOpen: (Reward) -> Unit) {
    val claimed = data.claimed
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (claimed.isEmpty()) {
            item {
                Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏆", fontSize = 64.sp)
                    Text(
                        "Tu pojawią się nagrody, na które już zapracowałeś.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        } else {
            item {
                val money = claimed.sumOf { data.savedGr(it.id) }
                val minutes = claimed.sumOf { data.minutes(it.id) }
                Text(
                    "Zdobyto ${claimed.size} nagród · ${formatMoney(money)} · ${formatMinutes(minutes)} pracy",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        items(claimed, key = { it.id }) { r ->
            Card(Modifier.fillMaxWidth().clickable { onOpen(r) }) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(r.emoji, fontSize = 32.sp)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(r.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            buildList {
                                add("Odebrano ${formatDate(r.claimedAt ?: 0)}")
                                if (r.needsMoney) add(formatMoney(data.savedGr(r.id)))
                                if (r.needsTime) add(formatMinutes(data.minutes(r.id)))
                            }.joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text("🏆", fontSize = 22.sp)
                }
            }
        }
    }
}

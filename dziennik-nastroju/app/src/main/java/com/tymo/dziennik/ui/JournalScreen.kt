@file:OptIn(ExperimentalLayoutApi::class)

package com.tymo.dziennik.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tymo.dziennik.Entry
import com.tymo.dziennik.JournalData
import com.tymo.dziennik.moodEmoji
import com.tymo.dziennik.moodLabel
import java.time.LocalDate
import java.time.format.TextStyle

@Composable
fun JournalScreen(data: JournalData, onOpen: (Entry) -> Unit) {
    val today = LocalDate.now()
    val week = (6 downTo 0).map { today.minusDays(it.toLong()) }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SectionCard("Ostatni tydzień") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    week.forEach { d ->
                        val e = data.entries[d.toString()]
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onOpen(e ?: Entry(date = d.toString())) },
                        ) {
                            Text(d.dayOfWeek.getDisplayName(TextStyle.SHORT, PL).take(3), style = MaterialTheme.typography.labelSmall)
                            Box(
                                Modifier.padding(top = 4.dp).size(38.dp).background(
                                    if (e != null) moodColor(e.mood).copy(alpha = 0.25f)
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape,
                                ),
                                contentAlignment = Alignment.Center,
                            ) { Text(e?.let { moodEmoji(it.mood) } ?: "·", fontSize = 20.sp) }
                        }
                    }
                }
                Text(
                    "Dotknij dnia, aby dodać lub poprawić wpis.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        if (data.entries.isEmpty()) {
            item {
                Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📔", fontSize = 56.sp)
                    Text(
                        "Zacznij od pierwszego wpisu. Wystarczy minuta dziennie, by lepiej rozumieć swoje samopoczucie.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        }

        items(data.sorted, key = { it.date }) { e -> EntryCard(e, Modifier.animateItem()) { onOpen(e) } }
    }
}

@Composable
private fun EntryCard(e: Entry, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).background(moodColor(e.mood).copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Text(moodEmoji(e.mood), fontSize = 24.sp) }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(longDate(e.localDate), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${moodLabel(e.mood)} · ⚡ ${e.energy}/5 · 😰 ${e.anxiety}/10 · 😴 ${e.sleepHours} h" +
                            if (e.medsTaken) " · 💊" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (e.symptoms.isNotEmpty()) {
                val shown = e.symptoms.take(4)
                FlowRow(
                    Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    shown.forEach { s -> Tag(s) }
                    if (e.symptoms.size > shown.size) Tag("+${e.symptoms.size - shown.size}")
                }
            }
            if (e.note.isNotBlank()) {
                Text(
                    e.note,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun Tag(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.onSecondaryContainer,
    )
}

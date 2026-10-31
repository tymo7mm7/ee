package com.tymo.kalendarz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tymo.kalendarz.AppData
import com.tymo.kalendarz.Occurrence
import com.tymo.kalendarz.occurrences
import java.time.LocalDate

@Composable
fun AgendaView(data: AppData, from: LocalDate, onOpen: (Occurrence) -> Unit) {
    val start = from.withDayOfMonth(1)
    val end = start.plusMonths(3).minusDays(1)
    val occ = occurrences(data.events, start, end, data.settings.holidays)
    val days = generateSequence(start) { it.plusDays(1) }.takeWhile { !it.isAfter(end) }
        .map { d -> d to occ.filter { it.coversDay(d) } }
        .filter { it.second.isNotEmpty() }
        .toList()
    val today = LocalDate.now()

    LazyColumn(contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 96.dp)) {
        if (days.isEmpty()) {
            item { Text("Brak wydarzeń w najbliższych 3 miesiącach.", Modifier.fillMaxWidth().padding(32.dp), textAlign = TextAlign.Center) }
        }
        items(days, key = { it.first.toEpochDay() }) { (d, list) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Column(Modifier.width(52.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(dayShort(d).uppercase(), fontSize = 11.sp, color = if (d == today) TodayBlue else MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(Modifier.size(34.dp).background(if (d == today) TodayBlue else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) {
                        Text("${d.dayOfMonth}", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = if (d == today) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                    if (d.dayOfMonth == 1 || d == days.first().first) {
                        Text(d.format(java.time.format.DateTimeFormatter.ofPattern("LLL", com.tymo.kalendarz.PL)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    list.forEach { o ->
                        Column(
                            Modifier.fillMaxWidth().background(eventColor(o.event.color), RoundedCornerShape(8.dp)).clickable { onOpen(o) }.padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Text(o.event.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            val sub = listOf(if (o.event.allDay) "Cały dzień" else if (o.multiDay) timeRange(o) else "${o.start.format(HM)} – ${o.end.format(HM)}", o.event.location)
                                .filter { it.isNotBlank() }.joinToString(" · ")
                            Text(sub, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

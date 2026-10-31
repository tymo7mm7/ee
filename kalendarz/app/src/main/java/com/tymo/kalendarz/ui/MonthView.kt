package com.tymo.kalendarz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tymo.kalendarz.AppData
import com.tymo.kalendarz.Occurrence
import com.tymo.kalendarz.occurrences
import com.tymo.kalendarz.weekStart
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MonthView(data: AppData, month: YearMonth, onDay: (LocalDate) -> Unit, onOpen: (Occurrence) -> Unit) {
    val first = weekStart(month.atDay(1))
    val last = weekStart(month.atEndOfMonth()).plusDays(6)
    val weeks = ((last.toEpochDay() - first.toEpochDay() + 1) / 7).toInt()
    val occ = occurrences(data.events, first, last, data.settings.holidays)
    val today = LocalDate.now()
    val line = MaterialTheme.colorScheme.outlineVariant

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            (0..6).forEach { i ->
                Text(
                    dayShort(first.plusDays(i.toLong())).uppercase(),
                    Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (i == 6) HolidayRed else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        (0 until weeks).forEach { w ->
            Row(Modifier.fillMaxWidth().weight(1f)) {
                (0..6).forEach { d ->
                    val day = first.plusDays((w * 7 + d).toLong())
                    val dayOcc = occ.filter { it.coversDay(day) }
                    val inMonth = day.month == month.month
                    val red = d == 6 || dayOcc.any { it.isHoliday }
                    BoxWithConstraints(
                        Modifier.weight(1f).fillMaxHeight().border(0.5.dp, line).clickable { onDay(day) },
                    ) {
                        val slots = ((maxHeight.value - 26) / 17).toInt().coerceAtLeast(0)
                        Column(Modifier.fillMaxSize().padding(horizontal = 2.dp, vertical = 2.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Box(
                                    Modifier.size(22.dp).background(if (day == today) TodayBlue else Color.Transparent, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "${day.dayOfMonth}",
                                        fontSize = 12.sp,
                                        fontWeight = if (day == today) FontWeight.Bold else FontWeight.Medium,
                                        color = when {
                                            day == today -> Color.White
                                            !inMonth -> MaterialTheme.colorScheme.outline
                                            red -> HolidayRed
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                    )
                                }
                            }
                            val shown = if (dayOcc.size > slots) (slots - 1).coerceAtLeast(0) else dayOcc.size
                            dayOcc.take(shown).forEach { o -> MonthChip(o, inMonth) { onOpen(o) } }
                            if (dayOcc.size > shown) {
                                Text("+${dayOcc.size - shown}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthChip(o: Occurrence, inMonth: Boolean, onClick: () -> Unit) {
    val c = eventColor(o.event.color).copy(alpha = if (inMonth) 1f else 0.5f)
    if (o.event.allDay || o.multiDay) {
        Text(
            o.event.title,
            Modifier.fillMaxWidth().background(c, RoundedCornerShape(3.dp)).clickable(onClick = onClick).padding(horizontal = 3.dp, vertical = 1.dp),
            color = Color.White, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Clip, fontWeight = FontWeight.Medium,
        )
    } else {
        Row(Modifier.fillMaxWidth().clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.padding(start = 1.dp, end = 2.dp).size(6.dp).background(c, CircleShape))
            Text(o.event.title, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Clip)
        }
    }
}

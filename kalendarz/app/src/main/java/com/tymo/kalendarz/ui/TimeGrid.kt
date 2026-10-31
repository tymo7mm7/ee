package com.tymo.kalendarz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tymo.kalendarz.AppData
import com.tymo.kalendarz.Occurrence
import com.tymo.kalendarz.occurrences
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

private val HOUR = 52.dp

private class Placed(val o: Occurrence, val startMin: Int, val endMin: Int) {
    var lane = 0
    var lanes = 1
}

/** Side-by-side layout for overlapping events (like the classic week view). */
private fun layout(list: List<Placed>): List<Placed> {
    val out = mutableListOf<Placed>()
    var cluster = mutableListOf<Placed>()
    var laneEnds = mutableListOf<Int>()
    var clusterEnd = -1
    fun flush() {
        cluster.forEach { it.lanes = laneEnds.size.coerceAtLeast(1) }
        out += cluster
        cluster = mutableListOf(); laneEnds = mutableListOf()
    }
    list.sortedWith(compareBy<Placed> { it.startMin }.thenByDescending { it.endMin }).forEach { p ->
        if (cluster.isNotEmpty() && p.startMin >= clusterEnd) flush()
        val visualEnd = maxOf(p.endMin, p.startMin + 25)
        val free = laneEnds.indexOfFirst { it <= p.startMin }
        if (free == -1) { p.lane = laneEnds.size; laneEnds += visualEnd } else { p.lane = free; laneEnds[free] = visualEnd }
        cluster += p
        clusterEnd = maxOf(clusterEnd, visualEnd)
    }
    flush()
    return out
}

@Composable
fun TimeGrid(
    data: AppData,
    days: List<LocalDate>,
    onOpen: (Occurrence) -> Unit,
    onEmpty: (LocalDate, Int) -> Unit,
    onDay: (LocalDate) -> Unit,
) {
    val occ = occurrences(data.events, days.first(), days.last(), data.settings.holidays)
    val today = LocalDate.now()
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    val hourPx = with(density) { HOUR.toPx() }
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        scroll.scrollTo(((LocalDateTime.now().hour - 1).coerceIn(0, 16) * hourPx).toInt())
        while (true) { delay(60_000); now = LocalDateTime.now() }
    }
    val gridLine = MaterialTheme.colorScheme.outlineVariant
    val single = days.size == 1

    Column(Modifier.fillMaxSize()) {
        // Day headers
        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.width(44.dp))
            days.forEach { d ->
                val isToday = d == today
                Column(Modifier.weight(1f).clickable { onDay(d) }.padding(vertical = 4.dp), horizontalAlignment = if (single) Alignment.Start else Alignment.CenterHorizontally) {
                    Text(dayShort(d).uppercase(), fontSize = 11.sp, color = if (isToday) TodayBlue else MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(Modifier.size(30.dp).background(if (isToday) TodayBlue else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) {
                        Text("${d.dayOfMonth}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
        // All-day and multi-day events
        val allDay = occ.filter { it.event.allDay || it.multiDay }
        if (allDay.isNotEmpty()) {
            Row(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                Box(Modifier.width(44.dp))
                days.forEach { d ->
                    Column(Modifier.weight(1f).padding(horizontal = 1.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        val list = allDay.filter { it.coversDay(d) }
                        list.take(3).forEach { o ->
                            Text(
                                o.event.title,
                                Modifier.fillMaxWidth().background(eventColor(o.event.color), RoundedCornerShape(4.dp)).clickable { onOpen(o) }.padding(horizontal = 4.dp, vertical = 2.dp),
                                color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (list.size > 3) Text("+${list.size - 3}", fontSize = 10.sp)
                    }
                }
            }
        }
        HorizontalDivider()
        // Hour grid
        Row(Modifier.fillMaxSize().verticalScroll(scroll)) {
            Column(Modifier.width(44.dp)) {
                (0..23).forEach { h ->
                    Box(Modifier.height(HOUR).fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                        if (h > 0) Text("%02d:00".format(h), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.offset(y = (-7).dp).padding(end = 4.dp))
                    }
                }
            }
            days.forEach { d ->
                val dayStart = d.atStartOfDay()
                val dayEnd = d.plusDays(1).atStartOfDay()
                val timed = occ.filter { !it.event.allDay && !it.multiDay && it.coversDay(d) }.map { o ->
                    val s = maxOf(o.start, dayStart)
                    val e = minOf(o.end, dayEnd)
                    Placed(o, ChronoUnit.MINUTES.between(dayStart, s).toInt(), ChronoUnit.MINUTES.between(dayStart, e).toInt())
                }
                val placed = layout(timed)
                BoxWithConstraints(
                    Modifier.weight(1f).height(HOUR * 24)
                        .drawBehind {
                            for (h in 0..24) drawLine(gridLine, Offset(0f, h * hourPx), Offset(size.width, h * hourPx), 1f)
                            drawLine(gridLine, Offset(0f, 0f), Offset(0f, size.height), 1f)
                        }
                        .pointerInput(d) { detectTapGestures { pos -> onEmpty(d, (pos.y / hourPx).toInt().coerceIn(0, 23)) } },
                ) {
                    val colW = maxWidth
                    placed.forEach { p ->
                        val top = HOUR * (p.startMin / 60f)
                        val height = (HOUR * ((p.endMin - p.startMin) / 60f)).coerceAtLeast(20.dp)
                        val w = colW / p.lanes
                        Column(
                            Modifier.offset(x = w * p.lane, y = top).width(w - 2.dp).height(height - 1.dp)
                                .background(eventColor(p.o.event.color), RoundedCornerShape(4.dp))
                                .clickable { onOpen(p.o) }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                        ) {
                            Text(p.o.event.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = if (height > 40.dp) 2 else 1, overflow = TextOverflow.Ellipsis)
                            if (height > 34.dp) {
                                Text(
                                    p.o.start.format(HM) + "–" + p.o.end.format(HM) + if (single && p.o.event.location.isNotBlank()) " · ${p.o.event.location}" else "",
                                    color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    if (d == today) {
                        val y = HOUR * ((now.hour * 60 + now.minute) / 60f)
                        Box(Modifier.offset(y = y - 1.dp).fillMaxWidth().height(2.dp).background(HolidayRed))
                        Box(Modifier.offset(x = (-5).dp, y = y - 5.dp).size(10.dp).background(HolidayRed, CircleShape))
                    }
                }
            }
        }
    }
}

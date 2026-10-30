package com.tymo.finanse.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

val PL: Locale = Locale("pl", "PL")
private val money = NumberFormat.getCurrencyInstance(PL)

fun fmt(gr: Long): String = money.format(gr / 100.0)
fun fmtShort(gr: Long): String = if (gr >= 100_000) "${gr / 100_000}k" else "${gr / 100}"

fun monthName(m: YearMonth): String =
    m.month.getDisplayName(TextStyle.FULL_STANDALONE, PL).replaceFirstChar { it.uppercase() } + " " + m.year

fun monthShort(m: YearMonth): String = m.month.getDisplayName(TextStyle.SHORT_STANDALONE, PL).take(3)

fun dayHeader(d: LocalDate): String {
    val today = LocalDate.now()
    return when (d) {
        today -> "Dzisiaj"
        today.minusDays(1) -> "Wczoraj"
        else -> d.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", PL)).replaceFirstChar { it.uppercase() }
    }
}

fun Long.color() = Color(this)

@Composable
fun MonthSwitcher(month: YearMonth, onChange: (YearMonth) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onChange(month.minusMonths(1)) }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Poprzedni miesiąc")
        }
        Text(
            monthName(month),
            Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        IconButton(onClick = { onChange(month.plusMonths(1)) }, enabled = month < YearMonth.now()) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Następny miesiąc")
        }
    }
}

/** Donut chart of [parts] (value, color). */
@Composable
fun Donut(parts: List<Pair<Long, Color>>, size: Dp, content: @Composable () -> Unit) {
    val total = parts.sumOf { it.first }.coerceAtLeast(1)
    val track = MaterialTheme.colorScheme.surfaceVariant
    val sweep by animateFloatAsState(1f, label = "donut")
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val s = 22.dp.toPx()
            val tl = Offset(s / 2, s / 2)
            val sz = Size(this.size.width - s, this.size.height - s)
            if (parts.isEmpty()) {
                drawArc(track, 0f, 360f, false, tl, sz, style = Stroke(s))
            } else {
                var start = -90f
                parts.forEach { (v, c) ->
                    val angle = 360f * v / total * sweep
                    drawArc(c, start, (angle - 1.5f).coerceAtLeast(0.5f), false, tl, sz, style = Stroke(s))
                    start += angle
                }
            }
        }
        content()
    }
}

/** Grouped bars: for each label, income (green) and expense (red). */
@Composable
fun IncomeExpenseBars(items: List<Triple<String, Long, Long>>) {
    val max = items.maxOfOrNull { maxOf(it.second, it.third) }?.coerceAtLeast(1) ?: 1
    Column {
        Canvas(Modifier.fillMaxWidth().height(150.dp)) {
            val slot = size.width / items.size
            val bw = slot * 0.3f
            val r = CornerRadius(4.dp.toPx())
            items.forEachIndexed { i, (_, inc, exp) ->
                val x = i * slot + slot * 0.18f
                val hi = size.height * inc / max
                val he = size.height * exp / max
                if (hi > 0) drawRoundRect(IncomeGreen, Offset(x, size.height - hi), Size(bw, hi), r)
                if (he > 0) drawRoundRect(ExpenseRed, Offset(x + bw + 2.dp.toPx(), size.height - he), Size(bw, he), r)
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            items.forEach { Text(it.first, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall) }
        }
    }
}

@Composable
fun SectionCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Column(Modifier.padding(top = 12.dp)) { content() }
        }
    }
}

@Composable
fun MoneyField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { v -> onChange(v.filter { it.isDigit() || it == ',' || it == '.' }.take(11)) },
        label = { Text(label) },
        suffix = { Text("zł") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth(),
    )
}

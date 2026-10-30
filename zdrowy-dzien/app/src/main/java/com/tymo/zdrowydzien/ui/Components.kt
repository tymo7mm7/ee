package com.tymo.zdrowydzien.ui

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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

val PL: Locale = Locale("pl", "PL")

fun shortDay(date: LocalDate): String = date.dayOfWeek.getDisplayName(TextStyle.SHORT, PL).take(3)

fun longDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", PL)).replaceFirstChar { it.uppercase() }

/** The last [n] days, oldest first, ending today. */
fun lastDays(n: Int): List<LocalDate> {
    val today = LocalDate.now()
    return (n - 1 downTo 0).map { today.minusDays(it.toLong()) }
}

@Composable
fun ProgressRing(
    progress: Float,
    color: Color,
    size: Dp,
    stroke: Dp = 12.dp,
    content: @Composable () -> Unit,
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), label = "ring")
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val s = stroke.toPx()
            val topLeft = Offset(s / 2, s / 2)
            val arcSize = Size(this.size.width - s, this.size.height - s)
            drawArc(color.copy(alpha = 0.18f), 0f, 360f, false, topLeft, arcSize, style = Stroke(s))
            drawArc(color, -90f, 360f * animated, false, topLeft, arcSize, style = Stroke(s, cap = StrokeCap.Round))
        }
        content()
    }
}

@Composable
fun BarChart(
    values: List<Float>,
    labels: List<String>,
    color: Color,
    modifier: Modifier = Modifier,
    goal: Float? = null,
) {
    val max = maxOf(values.maxOrNull() ?: 0f, goal ?: 0f).coerceAtLeast(1f)
    val goalColor = MaterialTheme.colorScheme.outline
    val track = color.copy(alpha = 0.12f)
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(130.dp)) {
            val slot = size.width / values.size
            val barW = slot * 0.55f
            val radius = CornerRadius(6.dp.toPx())
            values.forEachIndexed { i, v ->
                val x = i * slot + (slot - barW) / 2
                drawRoundRect(track, Offset(x, 0f), Size(barW, size.height), radius)
                val h = size.height * (v / max)
                if (h > 0f) drawRoundRect(color, Offset(x, size.height - h), Size(barW, h), radius)
            }
            goal?.let {
                val y = size.height * (1 - it / max)
                drawLine(
                    goalColor, Offset(0f, y), Offset(size.width, y),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f)),
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            labels.forEach {
                Text(
                    it, Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
fun SectionCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Box(Modifier.padding(top = 12.dp)) { content() }
        }
    }
}

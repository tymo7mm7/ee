package com.tymo.dziennik.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

val PL: Locale = Locale("pl", "PL")

fun longDate(d: LocalDate): String =
    d.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", PL)).replaceFirstChar { it.uppercase() }

fun shortDate(d: LocalDate): String = d.format(DateTimeFormatter.ofPattern("d MMM", PL))

data class Helpline(val name: String, val number: String, val info: String)

val helplines = listOf(
    Helpline("Numer alarmowy", "112", "Gdy życie jest zagrożone"),
    Helpline("Centrum Wsparcia dla osób w kryzysie psychicznym", "800702222", "800 70 2222 · całodobowo, bezpłatnie"),
    Helpline("Kryzysowy Telefon Zaufania", "116123", "116 123 · dla dorosłych"),
    Helpline("Telefon Zaufania dla Dzieci i Młodzieży", "116111", "116 111 · całodobowo"),
)

@Composable
fun CrisisCard(modifier: Modifier = Modifier, compact: Boolean = false) {
    val context = LocalContext.current
    Card(
        modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Nie jesteś z tym sam(a) 💜",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                "Jeśli jest Ci bardzo ciężko lub myślisz o zrobieniu sobie krzywdy, porozmawiaj z kimś teraz. " +
                    "Te numery są bezpłatne.",
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            (if (compact) helplines.take(2) else helplines).forEach { h ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(h.name, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onErrorContainer)
                        Text(h.info, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                    FilledTonalButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${h.number}")))
                    }) { Text("Zadzwoń") }
                }
            }
        }
    }
}

/** Simple line chart; null values are gaps (days without an entry). */
@Composable
fun LineChart(values: List<Float?>, min: Float, max: Float, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier.fillMaxWidth().height(140.dp)) {
        val n = values.size
        if (n < 2) return@Canvas
        val stepX = size.width / (n - 1)
        fun y(v: Float) = size.height * (1 - (v - min) / (max - min))
        for (i in 0..4) {
            val gy = size.height * i / 4f
            drawLine(grid, Offset(0f, gy), Offset(size.width, gy), 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
        }
        val path = Path()
        var started = false
        values.forEachIndexed { i, v ->
            if (v == null) {
                started = false
            } else {
                val p = Offset(i * stepX, y(v))
                if (started) path.lineTo(p.x, p.y) else path.moveTo(p.x, p.y)
                started = true
            }
        }
        drawPath(path, color, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
        values.forEachIndexed { i, v ->
            if (v != null) drawCircle(color, 4.dp.toPx(), Offset(i * stepX, y(v)))
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

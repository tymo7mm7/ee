package com.tymo.nagrody.ui

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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val PL: Locale = Locale("pl", "PL")

fun formatDate(ms: Long): String = SimpleDateFormat("d MMM yyyy", PL).format(Date(ms))
fun formatDateTime(ms: Long): String = SimpleDateFormat("d MMM, HH:mm", PL).format(Date(ms))

@Composable
fun GoalBar(icon: String, label: String, progress: Float, color: Color) {
    val animated by animateFloatAsState(progress, label = "bar")
    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row {
            Text("$icon $label", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.SemiBold, color = color)
        }
        LinearProgressIndicator(
            progress = { animated },
            modifier = Modifier.fillMaxWidth().height(8.dp).padding(top = 4.dp),
            color = color,
            trackColor = color.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round,
            drawStopIndicator = {},
        )
    }
}

@Composable
fun ProgressRing(progress: Float, color: Color, size: Dp, stroke: Dp = 14.dp, content: @Composable () -> Unit) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), label = "ring")
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val s = stroke.toPx()
            val tl = Offset(s / 2, s / 2)
            val sz = Size(this.size.width - s, this.size.height - s)
            drawArc(color.copy(alpha = 0.15f), 0f, 360f, false, tl, sz, style = Stroke(s))
            drawArc(color, -90f, 360f * animated, false, tl, sz, style = Stroke(s, cap = StrokeCap.Round))
        }
        content()
    }
}

@Composable
fun DecimalField(label: String, value: String, suffix: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { v -> onChange(v.filter { it.isDigit() || it == ',' || it == '.' }.take(9)) },
        label = { Text(label) },
        suffix = { Text(suffix) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
}

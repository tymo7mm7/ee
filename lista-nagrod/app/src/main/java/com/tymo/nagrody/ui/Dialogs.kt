@file:OptIn(ExperimentalLayoutApi::class)

package com.tymo.nagrody.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tymo.nagrody.GoalType
import com.tymo.nagrody.Kind
import com.tymo.nagrody.Reward
import com.tymo.nagrody.emojiChoices
import com.tymo.nagrody.formatMoney
import com.tymo.nagrody.parseMoney

private fun hoursText(min: Int): String =
    if (min == 0) "" else if (min % 60 == 0) "${min / 60}" else "%.1f".format(min / 60.0)

private fun parseHours(text: String): Int? {
    val v = text.trim().replace(',', '.').toDoubleOrNull() ?: return null
    if (v <= 0 || v > 10_000) return null
    return Math.round(v * 60).toInt()
}

@Composable
fun RewardEditor(initial: Reward, isNew: Boolean, onDismiss: () -> Unit, onSave: (Reward) -> Unit) {
    var name by remember { mutableStateOf(initial.name) }
    var emoji by remember { mutableStateOf(initial.emoji) }
    var goal by remember { mutableStateOf(initial.goalType) }
    var price by remember { mutableStateOf(if (initial.priceGr > 0) formatMoney(initial.priceGr).removeSuffix(" zł") else "") }
    var hours by remember { mutableStateOf(hoursText(initial.goalMinutes)) }

    val priceGr = parseMoney(price)
    val minutes = parseHours(hours)
    val valid = name.isNotBlank() && when (goal) {
        GoalType.MONEY -> priceGr != null
        GoalType.TIME -> minutes != null
        GoalType.BOTH -> priceGr != null && minutes != null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Nowa nagroda" else "Edytuj nagrodę") },
        text = {
            Column(
                Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(60) },
                    label = { Text("Nagroda, np. Książka „Wiedźmin”") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Ikona", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    emojiChoices.forEach { e ->
                        FilterChip(selected = emoji == e, onClick = { emoji = e }, label = { Text(e) })
                    }
                }
                Text("Jak zdobędziesz tę nagrodę?", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    GoalType.entries.forEach { g ->
                        val icon = when (g) { GoalType.MONEY -> "💰"; GoalType.TIME -> "⏱️"; GoalType.BOTH -> "💰+⏱️" }
                        FilterChip(selected = goal == g, onClick = { goal = g }, label = { Text("$icon ${g.label}") })
                    }
                }
                if (goal != GoalType.TIME) DecimalField("Cena / kwota do uzbierania", price, "zł") { price = it }
                if (goal != GoalType.MONEY) DecimalField("Czas nauki / pracy", hours, "h") { hours = it }
                Text(
                    when (goal) {
                        GoalType.MONEY -> "Odkładaj pieniądze, aż uzbierasz całą kwotę."
                        GoalType.TIME -> "Ucz się lub pracuj, aż wyrobisz wymagany czas."
                        GoalType.BOTH -> "Musisz uzbierać kwotę i przepracować czas."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = {
                onSave(
                    initial.copy(
                        name = name.trim(),
                        emoji = emoji,
                        goalType = goal,
                        priceGr = if (goal != GoalType.TIME) priceGr ?: 0 else 0,
                        goalMinutes = if (goal != GoalType.MONEY) minutes ?: 0 else 0,
                    )
                )
            }) { Text("Zapisz") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } },
    )
}

@Composable
fun ContributeDialog(reward: Reward, kind: Kind, onDismiss: () -> Unit, onAdd: (Long, String) -> Unit) {
    var value by remember { mutableStateOf("") }
    var note by remember { mutableStateOf(if (kind == Kind.TIME) "Nauka" else "") }
    val amount: Long? = if (kind == Kind.MONEY) parseMoney(value) else parseHours(value)?.toLong()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (kind == Kind.MONEY) "💰 Wpłata na: ${reward.name}" else "⏱️ Czas na: ${reward.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (kind == Kind.MONEY) {
                    DecimalField("Kwota", value, "zł") { value = it }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("5", "10", "20", "50", "100").forEach { v ->
                            AssistChip(onClick = { value = v }, label = { Text("$v zł") })
                        }
                    }
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it.take(40) },
                        label = { Text("Skąd? np. kieszonkowe, praca") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    DecimalField("Czas", value, "h") { value = it }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("0,25" to "15 min", "0,5" to "30 min", "1" to "1 h", "2" to "2 h", "4" to "4 h").forEach { (v, l) ->
                            AssistChip(onClick = { value = v }, label = { Text(l) })
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Nauka", "Praca", "Trening", "Obowiązki").forEach { l ->
                            FilterChip(selected = note == l, onClick = { note = l }, label = { Text(l) })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = amount != null, onClick = { amount?.let { onAdd(it, note.trim()) } }) { Text("Dodaj") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } },
    )
}

@Composable
fun CelebrationDialog(reward: Reward, onClaim: () -> Unit, onLater: () -> Unit) {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "scale",
    )
    AlertDialog(
        onDismissRequest = onLater,
        title = {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎉", fontSize = 56.sp, modifier = Modifier.scale(scale))
                Text("Udało się!", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Text(
                "Zasłużyłeś na nagrodę:\n${reward.emoji} ${reward.name}\n\nCiężka praca się opłaciła. Ciesz się!",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = { Button(onClick = onClaim) { Text("Odbieram! 🎁") } },
        dismissButton = { TextButton(onClick = onLater) { Text("Później") } },
    )
}

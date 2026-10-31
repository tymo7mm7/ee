@file:OptIn(ExperimentalLayoutApi::class)

package com.tymo.oceny.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tymo.oceny.AppEntry
import com.tymo.oceny.Kind
import com.tymo.oceny.emojiChoices

@Composable
fun Stars(rating: Int, size: Int = 18, onRate: ((Int) -> Unit)? = null) {
    Row {
        (1..5).forEach { i ->
            val mod = if (onRate != null) Modifier.clickable { onRate(if (rating == i) 0 else i) } else Modifier
            Text(if (i <= rating) "★" else "☆", fontSize = size.sp, color = if (i <= rating) StarGold else MaterialTheme.colorScheme.outline, modifier = mod)
        }
    }
}


/** Green/red bar: share of pluses vs. open minuses. */
@Composable
fun BalanceBar(app: AppEntry, modifier: Modifier = Modifier) {
    val plus = app.count(Kind.PLUS)
    val minus = app.open(Kind.MINUS)
    Row(modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
        if (plus + minus > 0) {
            if (plus > 0) Box(Modifier.weight(plus.toFloat()).fillMaxHeight().background(PlusGreen))
            if (minus > 0) Box(Modifier.weight(minus.toFloat()).fillMaxHeight().background(MinusRed))
        }
    }
}

fun shareText(context: Context, subject: String, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, subject))
}

@Composable
fun AppDialog(initial: AppEntry, isNew: Boolean, onDismiss: () -> Unit, onDelete: (() -> Unit)?, onSave: (AppEntry) -> Unit) {
    var name by remember { mutableStateOf(initial.name) }
    var emoji by remember { mutableStateOf(initial.emoji) }
    var folder by remember { mutableStateOf(initial.folder) }
    var summary by remember { mutableStateOf(initial.summary) }
    var confirm by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Nowa aplikacja" else "Edytuj aplikację") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it.take(50) }, label = { Text("Nazwa") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    emojiChoices.forEach { e -> FilterChip(selected = emoji == e, onClick = { emoji = e }, label = { Text(e) }) }
                }
                OutlinedTextField(folder, { folder = it.take(50) }, label = { Text("Folder w repozytorium (opcjonalnie)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(summary, { summary = it.take(500) }, label = { Text("Ogólne wrażenia (opcjonalnie)") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = { onSave(initial.copy(name = name.trim(), emoji = emoji, folder = folder.trim(), summary = summary.trim())) }) { Text("Zapisz") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) TextButton(onClick = { confirm = true }) { Text("Usuń", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text("Anuluj") }
            }
        },
    )
    if (confirm && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Usunąć „${initial.name}”?") },
            text = { Text("Wszystkie plusy, minusy i pomysły tej aplikacji zostaną usunięte.") },
            confirmButton = { TextButton(onClick = onDelete) { Text("Usuń", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Anuluj") } },
        )
    }
}

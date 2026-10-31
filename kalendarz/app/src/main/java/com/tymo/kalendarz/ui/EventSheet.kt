@file:OptIn(ExperimentalMaterial3Api::class)

package com.tymo.kalendarz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tymo.kalendarz.Occurrence
import com.tymo.kalendarz.Repeat
import com.tymo.kalendarz.reminderChoices

@Composable
fun EventSheet(o: Occurrence, onDismiss: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    var confirm by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(18.dp).background(eventColor(o.event.color), RoundedCornerShape(5.dp)))
                Text(o.event.title.ifBlank { "(bez tytułu)" }, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(start = 14.dp))
            }
            Text(
                if (o.multiDay) timeRange(o) else "${dateLong(o.firstDay)} · ${timeRange(o)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (o.isHoliday) Text("🇵🇱 Święto państwowe – dzień wolny od pracy")
            if (o.event.repeat != Repeat.NONE) Text("🔁 ${o.event.repeat.label}")
            o.event.reminder?.let { r -> Text("🔔 " + (reminderChoices.find { it.first == r }?.second ?: "$r min przed")) }
            if (o.event.location.isNotBlank()) Text("📍 ${o.event.location}")
            if (o.event.description.isNotBlank()) Text(o.event.description)
            if (!o.isHoliday) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    FilledTonalButton(onClick = onEdit, Modifier.weight(1f)) {
                        Icon(Icons.Default.Edit, null, Modifier.size(18.dp)); Text("  Edytuj", fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(onClick = { confirm = true }, Modifier.weight(1f)) {
                        Icon(Icons.Default.Delete, null, Modifier.size(18.dp)); Text("  Usuń")
                    }
                }
            }
        }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Usunąć wydarzenie?") },
            text = { Text(if (o.event.repeat != Repeat.NONE) "Zostaną usunięte wszystkie powtórzenia „${o.event.title}”." else "„${o.event.title}” zostanie usunięte.") },
            confirmButton = { TextButton(onClick = { confirm = false; onDelete() }) { Text("Usuń", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Anuluj") } },
        )
    }
}

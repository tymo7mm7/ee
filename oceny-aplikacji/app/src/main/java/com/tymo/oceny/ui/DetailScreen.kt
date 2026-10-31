@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.tymo.oceny.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tymo.oceny.AppEntry
import com.tymo.oceny.AppStatus
import com.tymo.oceny.AppViewModel
import com.tymo.oceny.Kind
import com.tymo.oceny.Note
import com.tymo.oceny.report

@Composable
fun DetailScreen(app: AppEntry, vm: AppViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    var kind by rememberSaveable { mutableStateOf(Kind.PLUS) }
    var text by rememberSaveable { mutableStateOf("") }
    var editingApp by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var exporting by remember { mutableStateOf(false) }

    fun add() {
        if (text.isNotBlank()) {
            vm.addNote(app.id, kind, text)
            text = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Wróć") } },
                title = { Text("${app.emoji} ${app.name}", maxLines = 1) },
                actions = {
                    IconButton(onClick = { exporting = true }) { Icon(Icons.Default.Share, "Udostępnij") }
                    IconButton(onClick = { editingApp = true }) { Icon(Icons.Default.Edit, "Edytuj") }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize().imePadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Twoja ocena", style = MaterialTheme.typography.labelLarge)
                        Stars(app.rating, 34) { vm.setRating(app.id, it) }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AppStatus.entries.forEach { s ->
                                FilterChip(selected = app.status == s, onClick = { vm.setStatus(app.id, s) }, label = { Text(s.label) })
                            }
                        }
                        if (app.summary.isNotBlank()) Text(app.summary, style = MaterialTheme.typography.bodyMedium)
                        if (app.folder.isNotBlank()) Text("Folder: ${app.folder}/", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        BalanceBar(app)
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                            Kind.entries.forEachIndexed { i, k ->
                                SegmentedButton(
                                    selected = kind == k,
                                    onClick = { kind = k },
                                    shape = SegmentedButtonDefaults.itemShape(i, Kind.entries.size),
                                    icon = {},
                                ) { Text("${k.emoji} ${k.label}") }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = text,
                                onValueChange = { text = it.take(500) },
                                placeholder = {
                                    Text(when (kind) {
                                        Kind.PLUS -> "Co Ci się podoba?"
                                        Kind.MINUS -> "Co nie działa lub przeszkadza?"
                                        Kind.IDEA -> "Co warto dodać?"
                                    })
                                },
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { add() }),
                                modifier = Modifier.weight(1f),
                            )
                            FilledIconButton(onClick = { add() }, enabled = text.isNotBlank(), modifier = Modifier.padding(start = 8.dp)) {
                                Icon(Icons.AutoMirrored.Filled.Send, "Dodaj")
                            }
                        }
                    }
                }
            }
            Kind.entries.forEach { k ->
                val list = app.notes.filter { it.kind == k }.sortedWith(compareBy<Note> { it.done }.thenByDescending { it.createdAt })
                item(key = "h$k") {
                    Text(
                        "${k.emoji} " + when (k) { Kind.PLUS -> "Plusy"; Kind.MINUS -> "Minusy"; Kind.IDEA -> "Pomysły" } + " (${list.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = kindColor(k),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    if (list.isEmpty()) Text("Brak – dodaj powyżej.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(list, key = { it.id }) { n ->
                    Row(
                        Modifier.fillMaxWidth().animateItem().clickable { editingNote = n }.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (k == Kind.PLUS) {
                            Text("•", color = PlusGreen, fontSize = 22.sp, modifier = Modifier.padding(horizontal = 14.dp))
                        } else {
                            Checkbox(checked = n.done, onCheckedChange = { vm.toggleDone(app.id, n.id) })
                        }
                        Text(
                            n.text,
                            Modifier.weight(1f),
                            textDecoration = if (n.done) TextDecoration.LineThrough else null,
                            color = if (n.done) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }

    if (editingApp) {
        AppDialog(app, isNew = false, onDismiss = { editingApp = false }, onDelete = { editingApp = false; vm.deleteApp(app.id); onBack() }) {
            vm.saveApp(it); editingApp = false
        }
    }
    editingNote?.let { n ->
        NoteDialog(
            n,
            onDismiss = { editingNote = null },
            onDelete = { vm.deleteNote(app.id, n.id); editingNote = null },
            onSave = { vm.saveNote(app.id, it); editingNote = null },
        )
    }
    if (exporting) {
        ExportDialog(onDismiss = { exporting = false }) { onlyOpen ->
            exporting = false
            shareText(context, "Ocena: ${app.name}", report(listOf(app), onlyOpen))
        }
    }
}

@Composable
private fun NoteDialog(initial: Note, onDismiss: () -> Unit, onDelete: () -> Unit, onSave: (Note) -> Unit) {
    var text by remember { mutableStateOf(initial.text) }
    var kind by remember { mutableStateOf(initial.kind) }
    var done by remember { mutableStateOf(initial.done) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj wpis") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Kind.entries.forEach { k -> FilterChip(selected = kind == k, onClick = { kind = k }, label = { Text("${k.emoji} ${k.label}") }) }
                }
                OutlinedTextField(text, { text = it.take(500) }, minLines = 3, modifier = Modifier.fillMaxWidth())
                if (kind != Kind.PLUS) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = done, onCheckedChange = { done = it })
                        Text(if (kind == Kind.MINUS) "Naprawione" else "Zrobione")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = text.isNotBlank(), onClick = { onSave(initial.copy(text = text.trim(), kind = kind, done = done && kind != Kind.PLUS)) }) { Text("Zapisz") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) { Text("Usuń", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text("Anuluj") }
            }
        },
    )
}

@file:OptIn(ExperimentalMaterial3Api::class)

package com.tymo.scenariusz.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tymo.scenariusz.AppViewModel
import com.tymo.scenariusz.Project
import com.tymo.scenariusz.Status

@Composable
fun ProjectsScreen(projects: List<Project>, vm: AppViewModel, onOpen: (Long) -> Unit) {
    var creating by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("🎬 Scenarzysta", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { creating = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nowy film") },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (projects.isEmpty()) {
                item {
                    Text(
                        "Zacznij swój pierwszy scenariusz – dodaj film przyciskiem poniżej.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                    )
                }
            }
            items(projects.sortedByDescending { it.updatedAt }, key = { it.id }) { p ->
                Card(Modifier.fillMaxWidth().clickable { onOpen(p.id) }) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎞️", fontSize = 28.sp)
                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(p.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (p.genre.isNotBlank()) Text(p.genre, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        if (p.logline.isNotBlank()) {
                            Text(p.logline, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 8.dp))
                        }
                        val ready = p.scenes.count { it.status != Status.DRAFT }
                        Text(
                            "${p.scenes.size} scen · ok. ${p.totalMinutes} min · ${p.characters.size} postaci · gotowe $ready/${p.scenes.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }
    }

    if (creating) {
        ProjectDialog(Project(id = vm.newId(), title = ""), isNew = true, onDismiss = { creating = false }) {
            vm.saveProject(it)
            creating = false
            onOpen(it.id)
        }
    }
}

@Composable
fun ProjectDialog(initial: Project, isNew: Boolean, onDismiss: () -> Unit, onDelete: (() -> Unit)? = null, onSave: (Project) -> Unit) {
    var title by remember { mutableStateOf(initial.title) }
    var genre by remember { mutableStateOf(initial.genre) }
    var logline by remember { mutableStateOf(initial.logline) }
    var author by remember { mutableStateOf(initial.author) }
    var confirmDelete by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Nowy film" else "Informacje o filmie") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(title, { title = it.take(80) }, label = { Text("Tytuł") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(genre, { genre = it.take(40) }, label = { Text("Gatunek, np. komedia, dramat") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(author, { author = it.take(60) }, label = { Text("Autor (opcjonalnie)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(logline, { logline = it.take(300) }, label = { Text("Logline – o czym jest film w 1–2 zdaniach") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(enabled = title.isNotBlank(), onClick = {
                onSave(initial.copy(title = title.trim(), genre = genre.trim(), logline = logline.trim(), author = author.trim()))
            }) { Text("Zapisz") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) TextButton(onClick = { confirmDelete = true }) { Text("Usuń film", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text("Anuluj") }
            }
        },
    )

    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Usunąć cały film?") },
            text = { Text("„${initial.title}” – wszystkie sceny i postacie zostaną trwale usunięte.") },
            confirmButton = { TextButton(onClick = onDelete) { Text("Usuń", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Anuluj") } },
        )
    }
}

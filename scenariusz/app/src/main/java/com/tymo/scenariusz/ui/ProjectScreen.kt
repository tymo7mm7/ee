@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.tymo.scenariusz.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tymo.scenariusz.AppViewModel
import com.tymo.scenariusz.Character
import com.tymo.scenariusz.PdfExport
import com.tymo.scenariusz.Project
import com.tymo.scenariusz.Scene
import com.tymo.scenariusz.ScriptPart
import com.tymo.scenariusz.Status
import com.tymo.scenariusz.scriptParts
import com.tymo.scenariusz.toPlainText

@Composable
fun ProjectScreen(
    project: Project,
    vm: AppViewModel,
    onBack: () -> Unit,
    onOpenScene: (Long) -> Unit,
    onNewScene: () -> Unit,
) {
    val context = LocalContext.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var menu by remember { mutableStateOf(false) }
    var editingInfo by remember { mutableStateOf(false) }
    var editingChar by remember { mutableStateOf<Character?>(null) }
    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Wróć") } },
                    title = { Text(project.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    actions = {
                        IconButton(onClick = { editingInfo = true }) { Icon(Icons.Default.Edit, "Informacje o filmie") }
                        IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, "Więcej") }
                        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                            DropdownMenuItem(text = { Text("📄 Eksportuj PDF") }, onClick = {
                                menu = false
                                runCatching { PdfExport.share(context, project) }
                                    .onFailure { Toast.makeText(context, "Nie udało się utworzyć PDF", Toast.LENGTH_SHORT).show() }
                            })
                            DropdownMenuItem(text = { Text("📝 Udostępnij jako tekst") }, onClick = {
                                menu = false
                                val send = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, project.title)
                                    putExtra(Intent.EXTRA_TEXT, project.toPlainText())
                                }
                                context.startActivity(Intent.createChooser(send, "Udostępnij scenariusz"))
                            })
                        }
                    },
                )
                PrimaryTabRow(selectedTabIndex = tab) {
                    listOf("Sceny", "Postacie", "Scenariusz").forEachIndexed { i, l ->
                        Tab(selected = tab == i, onClick = { tab = i }, text = { Text(l) })
                    }
                }
            }
        },
        floatingActionButton = {
            when (tab) {
                0 -> ExtendedFloatingActionButton(onClick = onNewScene, icon = { Icon(Icons.Default.Add, null) }, text = { Text("Nowa scena") })
                1 -> ExtendedFloatingActionButton(
                    onClick = { editingChar = Character(vm.newId(), "", color = project.characters.size) },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Nowa postać") },
                )
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (tab) {
                0 -> ScenesTab(project, onOpenScene, onMove = { id, d -> vm.moveScene(project.id, id, d) })
                1 -> CharactersTab(project) { editingChar = it }
                else -> ScriptTab(project)
            }
        }
    }

    if (editingInfo) {
        ProjectDialog(
            project, isNew = false,
            onDismiss = { editingInfo = false },
            onDelete = { editingInfo = false; vm.deleteProject(project.id) },
        ) { vm.saveProject(it); editingInfo = false }
    }

    editingChar?.let { c ->
        CharacterDialog(
            initial = c,
            isNew = project.characters.none { it.id == c.id },
            onDismiss = { editingChar = null },
            onDelete = { vm.deleteCharacter(project.id, c.id); editingChar = null },
            onSave = { vm.saveCharacter(project.id, it); editingChar = null },
        )
    }
}

@Composable
private fun ScenesTab(project: Project, onOpen: (Long) -> Unit, onMove: (Long, Int) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            val ready = project.scenes.count { it.status != Status.DRAFT }
            val int = project.scenes.count { it.place.name.startsWith("INT") }
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Stat("${project.scenes.size}", "scen")
                    Stat("~${project.totalMinutes} min", "czas")
                    Stat("$ready/${project.scenes.size}", "gotowe")
                    Stat("$int/${project.scenes.size - int}", "INT/EXT")
                }
            }
        }
        if (project.scenes.isEmpty()) {
            item { Text("Brak scen. Dodaj pierwszą scenę przyciskiem poniżej.", Modifier.padding(24.dp), textAlign = TextAlign.Center) }
        }
        itemsIndexed(project.scenes, key = { _, s -> s.id }) { i, s ->
            SceneCard(i + 1, s, project, Modifier.animateItem(), onClick = { onOpen(s.id) },
                onUp = if (i > 0) ({ onMove(s.id, -1) }) else null,
                onDown = if (i < project.scenes.lastIndex) ({ onMove(s.id, 1) }) else null)
        }
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
private fun SceneCard(number: Int, s: Scene, project: Project, modifier: Modifier, onClick: () -> Unit, onUp: (() -> Unit)?, onDown: (() -> Unit)?) {
    Card(modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(start = 14.dp, top = 12.dp, bottom = 12.dp)) {
            Box(
                Modifier.size(36.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) { Text("$number", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(s.heading, style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (s.title.isNotBlank()) Text(s.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (s.description.isNotBlank()) {
                    Text(s.description, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FlowRow(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${s.status.emoji} ${s.status.label}", style = MaterialTheme.typography.labelSmall)
                    Text("⏱ ${s.minutes} min", style = MaterialTheme.typography.labelSmall)
                    if (s.dialogue.isNotEmpty()) Text("💬 ${s.dialogue.size}", style = MaterialTheme.typography.labelSmall)
                    s.characterIds.mapNotNull { project.character(it) }.forEach { c ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).background(charColor(c.color), CircleShape))
                            Text(" ${c.name}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            Column {
                IconButton(onClick = { onUp?.invoke() }, enabled = onUp != null) { Icon(Icons.Default.KeyboardArrowUp, "W górę") }
                IconButton(onClick = { onDown?.invoke() }, enabled = onDown != null) { Icon(Icons.Default.KeyboardArrowDown, "W dół") }
            }
        }
    }
}

@Composable
private fun CharactersTab(project: Project, onEdit: (Character) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (project.characters.isEmpty()) {
            item { Text("Dodaj bohaterów filmu, aby przypisywać ich do scen i dialogów.", Modifier.padding(24.dp), textAlign = TextAlign.Center) }
        }
        items(project.characters, key = { it.id }) { c ->
            val scenes = project.scenes.count { c.id in it.characterIds }
            val lines = project.scenes.sumOf { s -> s.dialogue.count { it.characterId == c.id } }
            Card(Modifier.fillMaxWidth().clickable { onEdit(c) }) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).background(charColor(c.color), CircleShape), contentAlignment = Alignment.Center) {
                        Text(c.name.take(1).uppercase(), color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(c.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (c.description.isNotBlank()) Text(c.description, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text("Sceny: $scenes · kwestie: $lines", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/** Screenplay preview in a classic, monospace layout. */
@Composable
private fun ScriptTab(project: Project) {
    val mono = FontFamily.Monospace
    Card(
        Modifier.fillMaxSize().padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFFFFDF7)),
    ) {
        val ink = androidx.compose.ui.graphics.Color(0xFF1F2937)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)) {
            project.scriptParts().forEach { p ->
                when (p) {
                    is ScriptPart.Title -> Text(p.text, Modifier.fillMaxWidth().padding(top = 12.dp), fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 20.sp, textAlign = TextAlign.Center, color = ink)
                    is ScriptPart.Info -> Text(p.text, Modifier.fillMaxWidth().padding(top = 6.dp), fontFamily = mono, fontSize = 12.sp, textAlign = TextAlign.Center, color = ink, fontStyle = FontStyle.Italic)
                    is ScriptPart.Heading -> Text("${p.number}. ${p.text}", Modifier.padding(top = 22.dp), fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ink)
                    is ScriptPart.Action -> Text(p.text, Modifier.padding(top = 8.dp), fontFamily = mono, fontSize = 13.sp, color = ink)
                    is ScriptPart.Speaker -> Text(p.name, Modifier.fillMaxWidth().padding(top = 12.dp), fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center, color = ink)
                    is ScriptPart.Paren -> Text(p.text, Modifier.fillMaxWidth().padding(horizontal = 56.dp), fontFamily = mono, fontSize = 12.sp, textAlign = TextAlign.Center, color = ink, fontStyle = FontStyle.Italic)
                    is ScriptPart.Dialog -> Text(p.text, Modifier.fillMaxWidth().padding(horizontal = 36.dp), fontFamily = mono, fontSize = 13.sp, color = ink)
                }
            }
            if (project.scenes.isEmpty()) Text("\nTu pojawi się sformatowany scenariusz.", fontFamily = mono, color = ink)
        }
    }
}

@Composable
fun CharacterDialog(initial: Character, isNew: Boolean, onDismiss: () -> Unit, onDelete: () -> Unit, onSave: (Character) -> Unit) {
    var name by remember { mutableStateOf(initial.name) }
    var desc by remember { mutableStateOf(initial.description) }
    var color by remember { mutableIntStateOf(initial.color) }
    var confirmDelete by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Nowa postać" else "Edytuj postać") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it.take(40) }, label = { Text("Imię / nazwa") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(desc, { desc = it.take(300) }, label = { Text("Opis: wiek, charakter, cel") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                Text("Kolor", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    characterColors.indices.forEach { i ->
                        FilterChip(
                            selected = color % characterColors.size == i,
                            onClick = { color = i },
                            label = { Box(Modifier.size(16.dp).background(charColor(i), CircleShape)) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = { onSave(initial.copy(name = name.trim(), description = desc.trim(), color = color)) }) { Text("Zapisz") }
        },
        dismissButton = {
            Row {
                if (!isNew) TextButton(onClick = { confirmDelete = true }) { Text("Usuń", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text("Anuluj") }
            }
        },
    )
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Usunąć postać?") },
            text = { Text("${initial.name} zniknie ze wszystkich scen razem ze swoimi kwestiami.") },
            confirmButton = { TextButton(onClick = onDelete) { Text("Usuń", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Anuluj") } },
        )
    }
}

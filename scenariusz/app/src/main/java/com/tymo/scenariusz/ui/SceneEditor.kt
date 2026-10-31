@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.tymo.scenariusz.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tymo.scenariusz.Character
import com.tymo.scenariusz.Line
import com.tymo.scenariusz.Place
import com.tymo.scenariusz.Project
import com.tymo.scenariusz.Scene
import com.tymo.scenariusz.Status
import com.tymo.scenariusz.TimeOfDay
import kotlin.math.roundToInt

@Composable
fun SceneEditor(
    project: Project,
    initial: Scene,
    number: Int,
    newId: () -> Long,
    onClose: () -> Unit,
    onSave: (Scene) -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onAddCharacter: (Character) -> Unit,
) {
    var s by remember { mutableStateOf(initial) }
    var menu by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var confirmDiscard by remember { mutableStateOf(false) }
    var addingChar by remember { mutableStateOf(false) }
    val isNew = initial.id == -1L
    val close = { if (s != initial) confirmDiscard = true else onClose() }
    BackHandler(onBack = close)

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = close) { Icon(Icons.Default.Close, "Zamknij") } },
                title = { Text(if (isNew) "Nowa scena $number" else "Scena $number") },
                actions = {
                    TextButton(onClick = { onSave(s) }) { Text("Zapisz", fontWeight = FontWeight.Bold) }
                    if (!isNew) {
                        IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, "Więcej") }
                        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                            DropdownMenuItem(text = { Text("Duplikuj scenę") }, onClick = { menu = false; onSave(s); onDuplicate() })
                            DropdownMenuItem(text = { Text("Usuń scenę", color = MaterialTheme.colorScheme.error) }, onClick = { menu = false; confirmDelete = true })
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Live scene heading, as it will appear in the script.
            Text(
                "$number. ${s.heading}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            Section("Nagłówek sceny") {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    Place.entries.forEachIndexed { i, p ->
                        SegmentedButton(
                            selected = s.place == p,
                            onClick = { s = s.copy(place = p) },
                            shape = SegmentedButtonDefaults.itemShape(i, Place.entries.size),
                        ) { Text(p.label) }
                    }
                }
                OutlinedTextField(
                    value = s.location,
                    onValueChange = { s = s.copy(location = it.take(60)) },
                    label = { Text("Miejsce, np. Kuchnia, Las, Samochód Marka") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TimeOfDay.entries.forEach { t ->
                        FilterChip(selected = s.time == t, onClick = { s = s.copy(time = t) }, label = { Text(t.label) })
                    }
                }
            }

            Section("Opis sceny") {
                OutlinedTextField(
                    value = s.title,
                    onValueChange = { s = s.copy(title = it.take(80)) },
                    label = { Text("Krótki tytuł (dla Ciebie), np. „Kłótnia”") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = s.description,
                    onValueChange = { s = s.copy(description = it) },
                    label = { Text("Akcja – co widzimy i słyszymy") },
                    placeholder = { Text("Opisz miejsce, wygląd postaci, ich działania, ważne dźwięki…") },
                    minLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Section("Postacie w scenie") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    project.characters.forEach { c ->
                        val on = c.id in s.characterIds
                        FilterChip(
                            selected = on,
                            onClick = { s = s.copy(characterIds = if (on) s.characterIds - c.id else s.characterIds + c.id) },
                            label = { Text(c.name) },
                            leadingIcon = { Box(Modifier.size(10.dp).background(charColor(c.color), CircleShape)) },
                        )
                    }
                    AssistChip(onClick = { addingChar = true }, label = { Text("Nowa postać") }, leadingIcon = { Icon(Icons.Default.Add, null) })
                }
            }

            Section("Dialogi") {
                if (project.characters.isEmpty()) {
                    Text("Najpierw dodaj postać, aby pisać dialogi.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                s.dialogue.forEachIndexed { i, line ->
                    DialogueBlock(
                        line = line,
                        characters = project.characters,
                        onChange = { nl -> s = s.copy(dialogue = s.dialogue.toMutableList().also { it[i] = nl }) },
                        onDelete = { s = s.copy(dialogue = s.dialogue.filterNot { it.id == line.id }) },
                    )
                }
                if (project.characters.isNotEmpty()) {
                    OutlinedButton(
                        onClick = {
                            // Default speaker: someone other than the last one, when possible.
                            val last = s.dialogue.lastOrNull()?.characterId
                            val candidates = project.characters.filter { it.id in s.characterIds }.ifEmpty { project.characters }
                            val who = candidates.firstOrNull { it.id != last } ?: candidates.first()
                            s = s.copy(dialogue = s.dialogue + Line(newId(), who.id))
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("+ Dodaj kwestię") }
                }
            }

            Section("Produkcja") {
                Text("Szacowany czas: ${s.minutes} min", style = MaterialTheme.typography.labelLarge)
                Slider(value = s.minutes.toFloat(), onValueChange = { s = s.copy(minutes = it.roundToInt()) }, valueRange = 1f..20f, steps = 18)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Status.entries.forEach { st ->
                        FilterChip(selected = s.status == st, onClick = { s = s.copy(status = st) }, label = { Text("${st.emoji} ${st.label}") })
                    }
                }
                OutlinedTextField(
                    value = s.notes,
                    onValueChange = { s = s.copy(notes = it) },
                    label = { Text("Notatki (rekwizyty, muzyka, ujęcia…) – nie trafiają do scenariusza") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    if (addingChar) {
        CharacterDialog(
            initial = Character(newId(), "", color = project.characters.size),
            isNew = true,
            onDismiss = { addingChar = false },
            onDelete = {},
            onSave = { c ->
                onAddCharacter(c)
                s = s.copy(characterIds = s.characterIds + c.id)
                addingChar = false
            },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Usunąć scenę?") },
            text = { Text("Scena $number zostanie trwale usunięta.") },
            confirmButton = { TextButton(onClick = onDelete) { Text("Usuń", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Anuluj") } },
        )
    }

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text("Zapisać zmiany?") },
            text = { Text("Wprowadzone zmiany w scenie nie zostały zapisane.") },
            confirmButton = { TextButton(onClick = { confirmDiscard = false; onSave(s) }) { Text("Zapisz") } },
            dismissButton = { TextButton(onClick = { confirmDiscard = false; onClose() }) { Text("Odrzuć", color = MaterialTheme.colorScheme.error) } },
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun DialogueBlock(line: Line, characters: List<Character>, onChange: (Line) -> Unit, onDelete: () -> Unit) {
    val speaker = characters.find { it.id == line.characterId }
    Column(
        Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FlowRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                characters.forEach { c ->
                    FilterChip(
                        selected = c.id == line.characterId,
                        onClick = { onChange(line.copy(characterId = c.id)) },
                        label = { Text(c.name) },
                        leadingIcon = { Box(Modifier.size(8.dp).background(charColor(c.color), CircleShape)) },
                    )
                }
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Usuń kwestię", tint = MaterialTheme.colorScheme.outline) }
        }
        OutlinedTextField(
            value = line.parenthetical,
            onValueChange = { onChange(line.copy(parenthetical = it.take(60))) },
            label = { Text("Didaskalia, np. szeptem, ze złością (opcjonalnie)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = line.text,
            onValueChange = { onChange(line.copy(text = it)) },
            label = { Text("${speaker?.name ?: "Postać"} mówi…") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

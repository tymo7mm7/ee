@file:OptIn(ExperimentalMaterial3Api::class)

package com.tymo.oceny.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tymo.oceny.AppEntry
import com.tymo.oceny.AppViewModel
import com.tymo.oceny.Kind
import com.tymo.oceny.report

@Composable
fun AppRoot(vm: AppViewModel = viewModel()) {
    val apps by vm.apps.collectAsStateWithLifecycle()
    var openId by rememberSaveable { mutableStateOf<Long?>(null) }
    val open = openId?.let { id -> apps.find { it.id == id } }
    if (open != null) {
        BackHandler { openId = null }
        DetailScreen(open, vm, onBack = { openId = null })
    } else {
        ListScreen(apps, vm, onOpen = { openId = it })
    }
}

@Composable
private fun ListScreen(apps: List<AppEntry>, vm: AppViewModel, onOpen: (Long) -> Unit) {
    val context = LocalContext.current
    var sort by rememberSaveable { mutableIntStateOf(0) }
    var adding by remember { mutableStateOf<AppEntry?>(null) }
    var exporting by remember { mutableStateOf(false) }
    val sorted = when (sort) {
        1 -> apps.sortedByDescending { it.rating }
        2 -> apps.sortedByDescending { it.open(Kind.MINUS) }
        else -> apps.sortedByDescending { it.updatedAt }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("👍 Plusy i Minusy 👎", fontWeight = FontWeight.Bold) },
                actions = { IconButton(onClick = { exporting = true }) { Icon(Icons.Default.Share, "Eksportuj wszystko") } },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { adding = AppEntry(vm.newId(), "") },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Aplikacja") },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Total("👍", apps.sumOf { it.count(Kind.PLUS) }, "plusów")
                        Total("👎", apps.sumOf { it.open(Kind.MINUS) }, "do poprawy")
                        Total("💡", apps.sumOf { it.open(Kind.IDEA) }, "pomysłów")
                        Total("✅", apps.sumOf { a -> a.notes.count { it.done } }, "zrobione")
                    }
                }
                Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Ostatnio", "Ocena", "Najwięcej minusów").forEachIndexed { i, l ->
                        FilterChip(selected = sort == i, onClick = { sort = i }, label = { Text(l) })
                    }
                }
            }
            items(sorted, key = { it.id }) { a ->
                Card(Modifier.fillMaxWidth().animateItem().clickable { onOpen(a.id) }) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(a.emoji, fontSize = 30.sp)
                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(a.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Stars(a.rating, 15)
                                    Text("  ${a.status.label}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        Text(
                            "👍 ${a.count(Kind.PLUS)}   👎 ${a.open(Kind.MINUS)}" + (if (a.count(Kind.MINUS) > a.open(Kind.MINUS)) " (+${a.count(Kind.MINUS) - a.open(Kind.MINUS)} naprawione)" else "") + "   💡 ${a.open(Kind.IDEA)}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp, bottom = 6.dp),
                        )
                        BalanceBar(a)
                    }
                }
            }
        }
    }

    adding?.let { a ->
        AppDialog(a, isNew = true, onDismiss = { adding = null }, onDelete = null) {
            vm.saveApp(it); adding = null; onOpen(it.id)
        }
    }
    if (exporting) {
        ExportDialog(onDismiss = { exporting = false }) { onlyOpen ->
            exporting = false
            shareText(context, "Ocena aplikacji", report(apps, onlyOpen))
        }
    }
}

@Composable
private fun Total(icon: String, n: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$icon $n", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
fun ExportDialog(onDismiss: () -> Unit, onExport: (onlyOpen: Boolean) -> Unit) {
    var onlyOpen by remember { mutableStateOf(true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Udostępnij jako tekst") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Powstanie lista w formacie, który możesz wkleić np. do czatu z Claude, żeby poprawił aplikacje.")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Pomiń zrobione minusy i pomysły", Modifier.weight(1f))
                    Switch(checked = onlyOpen, onCheckedChange = { onlyOpen = it })
                }
            }
        },
        confirmButton = { Button(onClick = { onExport(onlyOpen) }) { Text("Udostępnij") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } },
    )
}

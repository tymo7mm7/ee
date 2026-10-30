@file:OptIn(ExperimentalMaterial3Api::class)

package com.tymo.finanse.ui

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tymo.finanse.AppViewModel
import com.tymo.finanse.Tx
import java.time.LocalDate
import java.time.YearMonth

private enum class Tab(val label: String, val icon: ImageVector) {
    OVERVIEW("Przegląd", Icons.Default.Home),
    LIST("Transakcje", Icons.Default.DateRange),
    BUDGETS("Budżety", Icons.Default.Warning),
    STATS("Statystyki", Icons.Default.Info),
}

@Composable
fun AppRoot(vm: AppViewModel) {
    val data by vm.data.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var monthText by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    val month = YearMonth.parse(monthText)
    var editing by remember { mutableStateOf<Tx?>(null) }
    val current = Tab.entries[tab]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (current == Tab.OVERVIEW) "Moje Finanse" else current.label, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Moje Finanse – eksport CSV")
                            putExtra(Intent.EXTRA_TEXT, data.toCsv())
                        }
                        context.startActivity(Intent.createChooser(send, "Eksportuj transakcje (CSV)"))
                    }) { Icon(Icons.Default.Share, "Eksport CSV") }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEachIndexed { i, t ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { Icon(t.icon, null) },
                        label = { Text(t.label) },
                    )
                }
            }
        },
        floatingActionButton = {
            if (current == Tab.OVERVIEW || current == Tab.LIST) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val date = if (month == YearMonth.now()) LocalDate.now() else month.atEndOfMonth()
                        editing = Tx(0, 0, false, "food", "", date.toString())
                    },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Dodaj") },
                )
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            val setMonth: (YearMonth) -> Unit = { monthText = it.toString() }
            when (current) {
                Tab.OVERVIEW -> OverviewScreen(data, month, setMonth, onOpen = { editing = it }, onSeeAll = { tab = Tab.LIST.ordinal })
                Tab.LIST -> TransactionsScreen(data, month, setMonth, onOpen = { editing = it })
                Tab.BUDGETS -> BudgetsScreen(data, vm)
                Tab.STATS -> StatsScreen(data)
            }
        }
    }

    editing?.let { t ->
        TxDialog(
            initial = t,
            isNew = t.id == 0L,
            onDismiss = { editing = null },
            onSave = { vm.saveTx(it); editing = null },
            onDelete = { vm.deleteTx(t.id); editing = null },
        )
    }
}

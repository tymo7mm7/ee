package com.tymo.finanse.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tymo.finanse.AppData
import com.tymo.finanse.Tx
import com.tymo.finanse.category
import java.time.YearMonth

@Composable
fun TransactionsScreen(data: AppData, month: YearMonth, onMonth: (YearMonth) -> Unit, onOpen: (Tx) -> Unit) {
    var filter by rememberSaveable { mutableIntStateOf(0) } // 0 all, 1 expenses, 2 income
    var query by rememberSaveable { mutableStateOf("") }
    val list = data.inMonth(month)
        .filter { filter == 0 || (filter == 1 && !it.income) || (filter == 2 && it.income) }
        .filter {
            query.isBlank() || it.note.contains(query, true) || category(it.categoryId).label.contains(query, true)
        }
    val byDay = list.groupBy { it.localDate }

    LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp)) {
        item { MonthSwitcher(month, onMonth) }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Szukaj…") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, "Wyczyść") } },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Wszystkie", "Wydatki", "Przychody").forEachIndexed { i, l ->
                    FilterChip(selected = filter == i, onClick = { filter = i }, label = { Text(l) })
                }
            }
        }
        if (list.isEmpty()) {
            item { Text("Brak transakcji.", Modifier.padding(top = 24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        byDay.forEach { (day, txs) ->
            item(key = "h$day") {
                Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp)) {
                    Text(dayHeader(day), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    val sum = txs.sumOf { it.signed }
                    Text(
                        (if (sum >= 0) "+" else "−") + fmt(kotlin.math.abs(sum)),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (sum >= 0) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                HorizontalDivider()
            }
            items(txs, key = { it.id }) { t -> TxRow(t) { onOpen(t) } }
        }
    }
}

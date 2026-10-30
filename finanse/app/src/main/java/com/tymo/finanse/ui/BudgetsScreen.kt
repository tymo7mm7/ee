package com.tymo.finanse.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tymo.finanse.AppViewModel
import com.tymo.finanse.AppData
import com.tymo.finanse.Recurring
import com.tymo.finanse.categories
import com.tymo.finanse.category
import java.time.YearMonth

@Composable
fun BudgetsScreen(data: AppData, vm: AppViewModel) {
    val month = YearMonth.now()
    var budgetFor by remember { mutableStateOf<String?>(null) }
    var editingRec by remember { mutableStateOf<Recurring?>(null) }
    val spent = data.spentBy(month).associate { it.first.id to it.second }
    val totalSpent = data.expense(month)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionCard("Limity na ${monthName(month).lowercase()}") {
            Text(
                "Ustaw, ile chcesz maksymalnie wydać. Pasek zrobi się żółty przy 80%, a czerwony po przekroczeniu.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            BudgetRow("💰 Wszystkie wydatki", totalSpent, data.budgets["ALL"]) { budgetFor = "ALL" }
            categories.filter { !it.income }.forEach { c ->
                BudgetRow("${c.emoji} ${c.label}", spent[c.id] ?: 0, data.budgets[c.id]) { budgetFor = c.id }
            }
        }

        SectionCard("🔁 Stałe opłaty i przychody") {
            Text(
                "Dodają się automatycznie co miesiąc w wybranym dniu, np. czynsz, telefon, Netflix czy wypłata.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            data.recurring.sortedBy { it.day }.forEach { r ->
                val c = category(r.categoryId)
                Row(
                    Modifier.fillMaxWidth().clickable { editingRec = r }.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(c.emoji)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(r.name, fontWeight = FontWeight.Medium)
                        Text("${r.day}. dnia miesiąca · ${c.label}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text((if (r.income) "+" else "−") + fmt(r.amountGr), color = if (r.income) IncomeGreen else ExpenseRed, fontWeight = FontWeight.SemiBold)
                }
            }
            val monthly = data.recurring.sumOf { if (it.income) it.amountGr else -it.amountGr }
            if (data.recurring.isNotEmpty()) {
                Text(
                    "Razem miesięcznie: ${(if (monthly >= 0) "+" else "−") + fmt(kotlin.math.abs(monthly))}",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            FilledTonalButton(
                onClick = { editingRec = Recurring(0, "", 0, false, "bills", 1, YearMonth.now().minusMonths(1).toString()) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) { Text("+ Dodaj stałą pozycję") }
        }
    }

    budgetFor?.let { key ->
        BudgetDialog(
            title = if (key == "ALL") "Limit wszystkich wydatków" else "Limit: ${category(key).emoji} ${category(key).label}",
            current = data.budgets[key],
            onDismiss = { budgetFor = null },
            onSave = { vm.setBudget(key, it); budgetFor = null },
        )
    }

    editingRec?.let { r ->
        RecurringDialog(
            initial = r,
            isNew = r.id == 0L,
            onDismiss = { editingRec = null },
            onSave = { vm.saveRecurring(it); editingRec = null },
            onDelete = { vm.deleteRecurring(r.id); editingRec = null },
        )
    }
}

@Composable
private fun BudgetRow(label: String, spent: Long, limit: Long?, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp)) {
        Row {
            Text(label, Modifier.weight(1f))
            Text(
                if (limit == null) "${fmt(spent)} · ustaw limit" else "${fmt(spent)} / ${fmt(limit)}",
                style = MaterialTheme.typography.bodySmall,
                color = if (limit != null && spent > limit) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (limit != null) FontWeight.SemiBold else null,
            )
        }
        if (limit != null) {
            val p = spent.toFloat() / limit
            LinearProgressIndicator(
                progress = { p.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).padding(top = 4.dp),
                color = budgetColor(p),
                strokeCap = StrokeCap.Round,
                drawStopIndicator = {},
            )
        }
    }
}

package com.tymo.finanse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tymo.finanse.AppData
import com.tymo.finanse.Tx
import com.tymo.finanse.category
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun OverviewScreen(
    data: AppData,
    month: YearMonth,
    onMonth: (YearMonth) -> Unit,
    onOpen: (Tx) -> Unit,
    onSeeAll: () -> Unit,
) {
    val income = data.income(month)
    val expense = data.expense(month)
    val spent = data.spentBy(month)
    val total = data.budgets["ALL"]

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MonthSwitcher(month, onMonth)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Saldo miesiąca", color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(
                    fmt(income - expense),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (income - expense >= 0) MaterialTheme.colorScheme.onPrimaryContainer else ExpenseRed,
                )
                Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("⬆ Przychody", style = MaterialTheme.typography.labelMedium)
                        Text(fmt(income), fontWeight = FontWeight.SemiBold, color = IncomeGreen)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("⬇ Wydatki", style = MaterialTheme.typography.labelMedium)
                        Text(fmt(expense), fontWeight = FontWeight.SemiBold, color = ExpenseRed)
                    }
                }
                Text(
                    "Łączne saldo konta: ${fmt(data.balance)}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        if (total != null) {
            val p = expense.toFloat() / total
            SectionCard("Budżet miesięczny") {
                LinearProgressIndicator(
                    progress = { p.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                    color = budgetColor(p),
                    strokeCap = StrokeCap.Round,
                    drawStopIndicator = {},
                )
                Text(
                    if (expense <= total) "Wydano ${fmt(expense)} z ${fmt(total)} · zostało ${fmt(total - expense)}"
                    else "Przekroczono budżet o ${fmt(expense - total)}!",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp),
                    color = if (expense > total) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (month == YearMonth.now() && expense < total) {
                    val daysLeft = month.lengthOfMonth() - LocalDate.now().dayOfMonth + 1
                    Text(
                        "Możesz wydawać ok. ${fmt((total - expense) / daysLeft)} dziennie do końca miesiąca.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        SectionCard("Na co wydajesz") {
            if (spent.isEmpty()) {
                Text("Brak wydatków w tym miesiącu.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Donut(spent.map { it.second to it.first.color.color() }, 140.dp) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Wydatki", style = MaterialTheme.typography.labelSmall)
                            Text(fmtShort(expense) + " zł", fontWeight = FontWeight.Bold)
                        }
                    }
                    Column(Modifier.weight(1f).padding(start = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        spent.take(6).forEach { (c, v) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(10.dp).background(c.color.color(), CircleShape))
                                Text(
                                    "${c.emoji} ${c.label}",
                                    Modifier.weight(1f).padding(start = 6.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text("${(v * 100 / expense.coerceAtLeast(1))}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        SectionCard("Ostatnie transakcje") {
            val recent = data.inMonth(month).take(5)
            if (recent.isEmpty()) Text("Dodaj pierwszą transakcję przyciskiem „Dodaj”.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            recent.forEach { TxRow(it) { onOpen(it) } }
            if (data.inMonth(month).size > 5) TextButton(onClick = onSeeAll) { Text("Zobacz wszystkie") }
        }
        Spacer(Modifier.height(88.dp))
    }
}

fun budgetColor(p: Float) = when {
    p >= 1f -> ExpenseRed
    p >= 0.8f -> WarnAmber
    else -> IncomeGreen
}

@Composable
fun TxRow(t: Tx, onClick: () -> Unit) {
    val c = category(t.categoryId)
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).background(c.color.color().copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center,
        ) { Text(c.emoji, fontSize = 20.sp) }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(t.note.ifBlank { c.label }, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
            Text(c.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            (if (t.income) "+" else "−") + fmt(t.amountGr),
            fontWeight = FontWeight.SemiBold,
            color = if (t.income) IncomeGreen else MaterialTheme.colorScheme.onSurface,
        )
    }
}

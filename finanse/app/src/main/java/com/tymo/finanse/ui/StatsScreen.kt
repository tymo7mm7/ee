package com.tymo.finanse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tymo.finanse.AppData
import com.tymo.finanse.category
import java.time.YearMonth

@Composable
fun StatsScreen(data: AppData) {
    val now = YearMonth.now()
    val months = (5 downTo 0).map { now.minusMonths(it.toLong()) }
    val bars = months.map { Triple(monthShort(it), data.income(it), data.expense(it)) }
    val totalIn = bars.sumOf { it.second }
    val totalOut = bars.sumOf { it.third }
    val active = bars.count { it.second > 0 || it.third > 0 }.coerceAtLeast(1)
    val savingRate = if (totalIn > 0) ((totalIn - totalOut) * 100 / totalIn) else 0

    val from = months.first().atDay(1).toString()
    val top = data.txs.filter { !it.income && it.date >= from }
        .groupBy { it.categoryId }
        .map { (id, l) -> category(id) to l.sumOf { it.amountGr } }
        .sortedByDescending { it.second }
    val biggest = data.txs.filter { !it.income && it.date >= from }.maxByOrNull { it.amountGr }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionCard("Ostatnie 6 miesięcy") {
            IncomeExpenseBars(bars)
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Legend(IncomeGreen, "Przychody")
                Legend(ExpenseRed, "Wydatki")
            }
        }
        SectionCard("Podsumowanie") {
            Stat("Przychody łącznie", fmt(totalIn))
            Stat("Wydatki łącznie", fmt(totalOut))
            Stat("Średnie wydatki / miesiąc", fmt(totalOut / active))
            Stat("Zaoszczędzono", fmt(totalIn - totalOut))
            Stat("Stopa oszczędności", "$savingRate%")
            biggest?.let { Stat("Największy wydatek", "${fmt(it.amountGr)} (${it.note.ifBlank { category(it.categoryId).label }})") }
            Text(
                when {
                    totalIn == 0L -> "Dodawaj przychody, aby zobaczyć stopę oszczędności."
                    savingRate >= 20 -> "Świetnie! Odkładasz ponad 20% przychodów 👏"
                    savingRate > 0 -> "Dobrze, że coś zostaje. Spróbuj odkładać 10–20% przychodów."
                    else -> "Wydajesz więcej, niż zarabiasz. Sprawdź największe kategorie poniżej."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        if (top.isNotEmpty()) {
            SectionCard("Największe kategorie (6 mies.)") {
                top.take(8).forEach { (c, v) ->
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Row {
                            Text("${c.emoji} ${c.label}", Modifier.weight(1f))
                            Text(fmt(v), fontWeight = FontWeight.SemiBold)
                        }
                        LinearProgressIndicator(
                            progress = { v.toFloat() / top.first().second },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            color = c.color.color(),
                            drawStopIndicator = {},
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Legend(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Text(label, Modifier.padding(start = 6.dp), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

package com.tymo.finanse

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.YearMonth

data class Category(val id: String, val label: String, val emoji: String, val color: Long, val income: Boolean)

val categories = listOf(
    Category("food", "Jedzenie", "🛒", 0xFFF97316, false),
    Category("eat", "Restauracje", "🍔", 0xFFFB923C, false),
    Category("home", "Mieszkanie", "🏠", 0xFF8B5CF6, false),
    Category("bills", "Rachunki", "💡", 0xFF6366F1, false),
    Category("transport", "Transport", "🚗", 0xFF3B82F6, false),
    Category("shopping", "Zakupy", "🛍️", 0xFFEC4899, false),
    Category("fun", "Rozrywka", "🎮", 0xFF14B8A6, false),
    Category("subs", "Subskrypcje", "📺", 0xFF0EA5E9, false),
    Category("health", "Zdrowie", "💊", 0xFFEF4444, false),
    Category("edu", "Edukacja", "📚", 0xFF84CC16, false),
    Category("gifts", "Prezenty", "🎁", 0xFFEAB308, false),
    Category("other", "Inne", "📦", 0xFF94A3B8, false),
    Category("salary", "Wypłata", "💼", 0xFF16A34A, true),
    Category("pocket", "Kieszonkowe", "🪙", 0xFF22C55E, true),
    Category("sale", "Sprzedaż", "🏷️", 0xFF10B981, true),
    Category("gift_in", "Prezent", "🎉", 0xFF059669, true),
    Category("other_in", "Inne przychody", "➕", 0xFF34D399, true),
)

fun category(id: String) = categories.find { it.id == id } ?: categories.first { it.id == "other" }

data class Tx(
    val id: Long,
    val amountGr: Long,          // always positive
    val income: Boolean,
    val categoryId: String,
    val note: String,
    val date: String,            // yyyy-MM-dd
) {
    val localDate: LocalDate get() = LocalDate.parse(date)
    val signed get() = if (income) amountGr else -amountGr
}

/** A fixed monthly payment or income that is added automatically. */
data class Recurring(
    val id: Long,
    val name: String,
    val amountGr: Long,
    val income: Boolean,
    val categoryId: String,
    val day: Int,                // 1..28
    val lastApplied: String?,    // yyyy-MM of the last month it was added
)

data class AppData(
    val txs: List<Tx> = emptyList(),
    /** Monthly limits: category id -> grosze; "ALL" is the total limit. */
    val budgets: Map<String, Long> = emptyMap(),
    val recurring: List<Recurring> = emptyList(),
) {
    fun inMonth(m: YearMonth) = txs.filter { YearMonth.from(it.localDate) == m }.sortedWith(compareByDescending<Tx> { it.date }.thenByDescending { it.id })
    fun income(m: YearMonth) = inMonth(m).filter { it.income }.sumOf { it.amountGr }
    fun expense(m: YearMonth) = inMonth(m).filter { !it.income }.sumOf { it.amountGr }
    fun spentBy(m: YearMonth): List<Pair<Category, Long>> =
        inMonth(m).filter { !it.income }.groupBy { it.categoryId }
            .map { (id, list) -> category(id) to list.sumOf { it.amountGr } }
            .sortedByDescending { it.second }
    val balance get() = txs.sumOf { it.signed }

    /** Adds every recurring item that became due since it was last applied. */
    fun applyRecurring(today: LocalDate = LocalDate.now()): AppData {
        val now = YearMonth.from(today)
        var nextId = (txs.maxOfOrNull { it.id } ?: 0L) + 1
        val newTxs = mutableListOf<Tx>()
        val updated = recurring.map { r ->
            var month = r.lastApplied?.let { YearMonth.parse(it).plusMonths(1) } ?: now
            var last = r.lastApplied
            while (!month.isAfter(now)) {
                val day = minOf(r.day, month.lengthOfMonth())
                val date = month.atDay(day)
                if (date.isAfter(today)) break
                newTxs += Tx(nextId++, r.amountGr, r.income, r.categoryId, "🔁 ${r.name}", date.toString())
                last = month.toString()
                month = month.plusMonths(1)
            }
            r.copy(lastApplied = last)
        }
        return if (newTxs.isEmpty() && updated == recurring) this else copy(txs = txs + newTxs, recurring = updated)
    }

    fun toJson(): String = JSONObject().apply {
        put("txs", JSONArray().also { a ->
            txs.forEach {
                a.put(JSONObject().put("id", it.id).put("amount", it.amountGr).put("income", it.income)
                    .put("cat", it.categoryId).put("note", it.note).put("date", it.date))
            }
        })
        put("budgets", JSONObject().also { o -> budgets.forEach { (k, v) -> o.put(k, v) } })
        put("recurring", JSONArray().also { a ->
            recurring.forEach {
                a.put(JSONObject().apply {
                    put("id", it.id); put("name", it.name); put("amount", it.amountGr); put("income", it.income)
                    put("cat", it.categoryId); put("day", it.day); it.lastApplied?.let { l -> put("last", l) }
                })
            }
        })
    }.toString()

    fun toCsv(): String = buildString {
        appendLine("data;typ;kategoria;kwota;opis")
        txs.sortedBy { it.date }.forEach {
            val amount = "%.2f".format(it.amountGr / 100.0).replace('.', ',')
            appendLine("${it.date};${if (it.income) "przychód" else "wydatek"};${category(it.categoryId).label};${if (it.income) "" else "-"}$amount;${it.note.replace(';', ',')}")
        }
    }

    companion object {
        fun fromJson(json: String): AppData {
            val o = JSONObject(json)
            val ta = o.optJSONArray("txs") ?: JSONArray()
            val txs = (0 until ta.length()).map {
                val t = ta.getJSONObject(it)
                Tx(t.getLong("id"), t.getLong("amount"), t.getBoolean("income"), t.getString("cat"), t.optString("note", ""), t.getString("date"))
            }
            val bo = o.optJSONObject("budgets") ?: JSONObject()
            val budgets = bo.keys().asSequence().associateWith { bo.getLong(it) }
            val ra = o.optJSONArray("recurring") ?: JSONArray()
            val recurring = (0 until ra.length()).map {
                val r = ra.getJSONObject(it)
                Recurring(
                    r.getLong("id"), r.getString("name"), r.getLong("amount"), r.getBoolean("income"),
                    r.getString("cat"), r.getInt("day"), if (r.has("last")) r.getString("last") else null,
                )
            }
            return AppData(txs, budgets, recurring)
        }

        fun sample(): AppData {
            val t = LocalDate.now()
            fun d(daysAgo: Long) = t.minusDays(daysAgo).toString()
            return AppData(
                txs = listOf(
                    Tx(1, 450000, true, "salary", "Pensja", t.withDayOfMonth(1).toString()),
                    Tx(2, 18750, false, "food", "Zakupy spożywcze", d(0)),
                    Tx(3, 4200, false, "eat", "Pizza", d(1)),
                    Tx(4, 12000, false, "transport", "Paliwo", d(2)),
                    Tx(5, 8999, false, "shopping", "Koszulka", d(3)),
                ),
                budgets = mapOf("ALL" to 300000L, "food" to 100000L, "eat" to 30000L, "fun" to 20000L),
                recurring = listOf(
                    Recurring(1, "Telefon", 4500, false, "bills", 10, YearMonth.now().minusMonths(1).toString()),
                    Recurring(2, "Netflix", 4300, false, "subs", 15, YearMonth.now().minusMonths(1).toString()),
                ),
            )
        }
    }
}

/** Parses "12", "12,5" or "1 234.50" into grosze. */
fun parseMoney(text: String): Long? {
    val v = text.replace(" ", "").replace(',', '.').toDoubleOrNull() ?: return null
    if (v <= 0 || v > 100_000_000) return null
    return Math.round(v * 100)
}

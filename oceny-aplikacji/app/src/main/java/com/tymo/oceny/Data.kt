package com.tymo.oceny

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class Kind(val label: String, val emoji: String) {
    PLUS("Plus", "👍"),
    MINUS("Minus", "👎"),
    IDEA("Pomysł", "💡"),
}

enum class AppStatus(val label: String) {
    TESTING("Testuję"),
    USING("Używam"),
    DROPPED("Porzucona"),
}

data class Note(
    val id: Long,
    val kind: Kind,
    val text: String,
    val done: Boolean = false,      // minus fixed / idea implemented
    val createdAt: Long = System.currentTimeMillis(),
)

data class AppEntry(
    val id: Long,
    val name: String,
    val emoji: String = "📱",
    val folder: String = "",
    val rating: Int = 0,             // 0 = not rated, 1..5 stars
    val status: AppStatus = AppStatus.TESTING,
    val summary: String = "",
    val notes: List<Note> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    fun count(k: Kind) = notes.count { it.kind == k }
    fun open(k: Kind) = notes.count { it.kind == k && !it.done }
}

val emojiChoices = listOf("📱", "✅", "💧", "💜", "🎁", "💰", "🎬", "📅", "🎮", "📚", "🛒", "🏋️", "🎵", "📷", "🗺️", "🔧")

/** The apps already built in this repository, so you can start rating them right away. */
fun startingApps(): List<AppEntry> = listOf(
    AppEntry(1, "Zadania", "✅", "android"),
    AppEntry(2, "Zdrowy Dzień", "💧", "zdrowy-dzien"),
    AppEntry(3, "Mój Dziennik", "💜", "dziennik-nastroju"),
    AppEntry(4, "Lista Nagród", "🎁", "lista-nagrod"),
    AppEntry(5, "Moje Finanse", "💰", "finanse"),
    AppEntry(6, "Scenarzysta", "🎬", "scenariusz"),
    AppEntry(7, "Mój Kalendarz", "📅", "kalendarz"),
)

fun List<AppEntry>.toJson(): String = JSONArray().also { arr ->
    forEach { a ->
        arr.put(JSONObject().apply {
            put("id", a.id); put("name", a.name); put("emoji", a.emoji); put("folder", a.folder)
            put("rating", a.rating); put("status", a.status.name); put("summary", a.summary); put("updated", a.updatedAt)
            put("notes", JSONArray().also { na ->
                a.notes.forEach { n ->
                    na.put(JSONObject().put("id", n.id).put("kind", n.kind.name).put("text", n.text).put("done", n.done).put("at", n.createdAt))
                }
            })
        })
    }
}.toString()

fun appsFromJson(json: String): List<AppEntry> {
    val arr = JSONArray(json)
    return (0 until arr.length()).map { i ->
        val o = arr.getJSONObject(i)
        val na = o.optJSONArray("notes") ?: JSONArray()
        AppEntry(
            id = o.getLong("id"),
            name = o.optString("name"),
            emoji = o.optString("emoji", "📱"),
            folder = o.optString("folder"),
            rating = o.optInt("rating"),
            status = runCatching { AppStatus.valueOf(o.optString("status")) }.getOrDefault(AppStatus.TESTING),
            summary = o.optString("summary"),
            updatedAt = o.optLong("updated"),
            notes = (0 until na.length()).map {
                val n = na.getJSONObject(it)
                Note(
                    n.getLong("id"),
                    runCatching { Kind.valueOf(n.optString("kind")) }.getOrDefault(Kind.PLUS),
                    n.optString("text"), n.optBoolean("done"), n.optLong("at"),
                )
            },
        )
    }
}

private fun stars(n: Int) = if (n == 0) "brak oceny" else "★".repeat(n) + "☆".repeat(5 - n)

/** Text report, e.g. to paste into a chat and ask for fixes. */
fun report(apps: List<AppEntry>, onlyOpen: Boolean): String = buildString {
    val date = SimpleDateFormat("d.MM.yyyy", Locale("pl", "PL")).format(Date())
    appendLine("# Ocena aplikacji – $date")
    apps.forEach { a ->
        val notes = a.notes.filter { !onlyOpen || !it.done || it.kind == Kind.PLUS }
        appendLine()
        appendLine("## ${a.emoji} ${a.name}" + if (a.folder.isNotBlank()) " (folder `${a.folder}/`)" else "")
        appendLine("Ocena: ${stars(a.rating)} · Status: ${a.status.label}")
        if (a.summary.isNotBlank()) appendLine("Podsumowanie: ${a.summary}")
        Kind.entries.forEach { k ->
            val list = notes.filter { it.kind == k }
            if (list.isNotEmpty()) {
                appendLine(when (k) { Kind.PLUS -> "Plusy:"; Kind.MINUS -> "Minusy do poprawy:"; Kind.IDEA -> "Pomysły:" })
                list.forEach { appendLine("- " + (if (it.done) "[zrobione] " else "") + it.text) }
            }
        }
        if (notes.isEmpty()) appendLine("(brak notatek)")
    }
}

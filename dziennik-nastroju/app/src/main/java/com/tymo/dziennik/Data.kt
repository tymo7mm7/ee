package com.tymo.dziennik

import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.time.LocalDate

data class Entry(
    val date: String,              // "yyyy-MM-dd", one entry per day
    val mood: Int = 3,             // 1..5
    val energy: Int = 3,           // 1..5
    val anxiety: Int = 0,          // 0..10
    val sleepHours: Float = 7f,
    val symptoms: Set<String> = emptySet(),
    val medsTaken: Boolean = false,
    val note: String = "",
    val helped: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
) {
    val localDate: LocalDate get() = LocalDate.parse(date)
}

data class Prefs(
    val pinHash: String? = null,
    val reminder: Boolean = false,
    val reminderHour: Int = 20,
    val customSymptoms: List<String> = emptyList(),
)

data class JournalData(
    val entries: Map<String, Entry> = emptyMap(),
    val prefs: Prefs = Prefs(),
) {
    val sorted: List<Entry> get() = entries.values.sortedByDescending { it.date }
    val allSymptoms: List<String> get() = (defaultSymptoms + prefs.customSymptoms).distinct()
}

const val CRISIS_SYMPTOM = "Myśli rezygnacyjne"

val defaultSymptoms = listOf(
    "Lęk", "Smutek", "Drażliwość", "Zmęczenie", "Bezsenność", "Nadmierna senność",
    "Ból głowy", "Napięcie mięśni", "Kołatanie serca", "Problemy z koncentracją",
    "Brak apetytu", "Objadanie się", "Płaczliwość", "Brak motywacji", "Ataki paniki",
    "Poczucie osamotnienia", "Gonitwa myśli", "Ból brzucha", CRISIS_SYMPTOM,
)

fun moodEmoji(m: Int) = when (m) {
    1 -> "😢"; 2 -> "🙁"; 3 -> "😐"; 4 -> "🙂"; else -> "😄"
}

fun moodLabel(m: Int) = when (m) {
    1 -> "Bardzo źle"; 2 -> "Źle"; 3 -> "Neutralnie"; 4 -> "Dobrze"; else -> "Bardzo dobrze"
}

fun energyLabel(e: Int) = when (e) {
    1 -> "Brak sił"; 2 -> "Niska"; 3 -> "Średnia"; 4 -> "Dobra"; else -> "Pełna energii"
}

fun sha256(text: String): String =
    MessageDigest.getInstance("SHA-256").digest(text.toByteArray())
        .joinToString("") { "%02x".format(it) }

fun Entry.toJson(): JSONObject = JSONObject().apply {
    put("date", date); put("mood", mood); put("energy", energy); put("anxiety", anxiety)
    put("sleep", sleepHours.toDouble()); put("symptoms", JSONArray(symptoms.toList()))
    put("meds", medsTaken); put("note", note); put("helped", helped); put("updatedAt", updatedAt)
}

fun entryFromJson(o: JSONObject): Entry {
    val s = o.optJSONArray("symptoms") ?: JSONArray()
    return Entry(
        date = o.getString("date"),
        mood = o.optInt("mood", 3),
        energy = o.optInt("energy", 3),
        anxiety = o.optInt("anxiety", 0),
        sleepHours = o.optDouble("sleep", 7.0).toFloat(),
        symptoms = (0 until s.length()).map { s.getString(it) }.toSet(),
        medsTaken = o.optBoolean("meds", false),
        note = o.optString("note", ""),
        helped = o.optString("helped", ""),
        updatedAt = o.optLong("updatedAt", 0L),
    )
}

fun JournalData.toJson(): String = JSONObject().apply {
    put("entries", JSONArray().also { a -> entries.values.forEach { a.put(it.toJson()) } })
    put("prefs", JSONObject().apply {
        prefs.pinHash?.let { put("pin", it) }
        put("reminder", prefs.reminder)
        put("reminderHour", prefs.reminderHour)
        put("custom", JSONArray(prefs.customSymptoms))
    })
}.toString()

fun journalFromJson(json: String): JournalData {
    val o = JSONObject(json)
    val arr = o.optJSONArray("entries") ?: JSONArray()
    val entries = (0 until arr.length()).map { entryFromJson(arr.getJSONObject(it)) }.associateBy { it.date }
    val p = o.optJSONObject("prefs") ?: JSONObject()
    val custom = p.optJSONArray("custom") ?: JSONArray()
    return JournalData(
        entries,
        Prefs(
            pinHash = if (p.has("pin")) p.getString("pin") else null,
            reminder = p.optBoolean("reminder", false),
            reminderHour = p.optInt("reminderHour", 20),
            customSymptoms = (0 until custom.length()).map { custom.getString(it) },
        ),
    )
}

/** Plain-text summary of recent entries, e.g. to show a doctor or therapist. */
fun JournalData.report(days: Int = 30): String {
    val from = LocalDate.now().minusDays(days.toLong() - 1)
    val list = sorted.filter { !it.localDate.isBefore(from) }
    if (list.isEmpty()) return "Brak wpisów z ostatnich $days dni."
    val sb = StringBuilder()
    sb.appendLine("Mój Dziennik – podsumowanie ostatnich $days dni")
    sb.appendLine("Liczba wpisów: ${list.size}")
    sb.appendLine("Średni nastrój: ${"%.1f".format(list.map { it.mood }.average())} / 5")
    sb.appendLine("Średni lęk: ${"%.1f".format(list.map { it.anxiety }.average())} / 10")
    sb.appendLine("Średni sen: ${"%.1f".format(list.map { it.sleepHours }.average())} h")
    val freq = list.flatMap { it.symptoms }.groupingBy { it }.eachCount().entries.sortedByDescending { it.value }
    if (freq.isNotEmpty()) {
        sb.appendLine("Najczęstsze objawy: " + freq.take(8).joinToString { "${it.key} (${it.value}×)" })
    }
    sb.appendLine()
    list.forEach { e ->
        sb.appendLine("${e.date} – nastrój ${e.mood}/5 (${moodLabel(e.mood)}), energia ${e.energy}/5, lęk ${e.anxiety}/10, sen ${e.sleepHours} h" +
            if (e.medsTaken) ", leki przyjęte" else "")
        if (e.symptoms.isNotEmpty()) sb.appendLine("  Objawy: ${e.symptoms.joinToString()}")
        if (e.note.isNotBlank()) sb.appendLine("  Notatka: ${e.note}")
        if (e.helped.isNotBlank()) sb.appendLine("  Co pomogło / zaszkodziło: ${e.helped}")
    }
    return sb.toString()
}

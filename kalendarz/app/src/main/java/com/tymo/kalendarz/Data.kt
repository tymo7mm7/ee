package com.tymo.kalendarz

import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

val PL: Locale = Locale("pl", "PL")

data class CalColor(val name: String, val argb: Long)

/** Event colors, similar to the classic calendar palette. */
val calColors = listOf(
    CalColor("Pomidorowy", 0xFFD50000),
    CalColor("Flaming", 0xFFE67C73),
    CalColor("Mandarynkowy", 0xFFF4511E),
    CalColor("Bananowy", 0xFFF6BF26),
    CalColor("Szałwia", 0xFF33B679),
    CalColor("Bazylia", 0xFF0B8043),
    CalColor("Pawi", 0xFF039BE5),
    CalColor("Jagodowy", 0xFF3F51B5),
    CalColor("Lawendowy", 0xFF7986CB),
    CalColor("Winogronowy", 0xFF8E24AA),
    CalColor("Grafitowy", 0xFF616161),
)
const val DEFAULT_COLOR = 6
const val HOLIDAY_COLOR = 5

enum class Repeat(val label: String) {
    NONE("Nie powtarza się"),
    DAILY("Codziennie"),
    WEEKLY("Co tydzień"),
    MONTHLY("Co miesiąc"),
    YEARLY("Co roku"),
}

/** Minutes before start; null = no reminder. */
val reminderChoices: List<Pair<Int?, String>> = listOf(
    null to "Brak",
    0 to "W chwili rozpoczęcia",
    10 to "10 min przed",
    30 to "30 min przed",
    60 to "1 godz. przed",
    1440 to "1 dzień przed",
)

data class Event(
    val id: Long,
    val title: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val allDay: Boolean = false,
    val color: Int = DEFAULT_COLOR,
    val location: String = "",
    val description: String = "",
    val repeat: Repeat = Repeat.NONE,
    val reminder: Int? = null,
)

/** One concrete appearance of an event (a repeating event has many). */
data class Occurrence(val event: Event, val start: LocalDateTime, val end: LocalDateTime) {
    val isHoliday get() = event.id < 0
    val firstDay: LocalDate get() = start.toLocalDate()
    /** Last day it covers; an event ending exactly at midnight does not cover the next day. */
    val lastDay: LocalDate
        get() = if (!event.allDay && end.toLocalTime() == LocalTime.MIDNIGHT && end.isAfter(start)) end.toLocalDate().minusDays(1)
        else end.toLocalDate()

    fun coversDay(d: LocalDate) = !d.isBefore(firstDay) && !d.isAfter(lastDay)
    val multiDay get() = lastDay.isAfter(firstDay)
}

fun weekStart(d: LocalDate): LocalDate = d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

/** All occurrences touching the days [from]..[to] (inclusive), sorted. */
fun occurrences(events: List<Event>, from: LocalDate, to: LocalDate, holidays: Boolean): List<Occurrence> {
    val out = mutableListOf<Occurrence>()
    for (e in events) {
        val length = Duration(e.start, e.end)
        fun at(k: Long): LocalDateTime = when (e.repeat) {
            Repeat.NONE -> e.start
            Repeat.DAILY -> e.start.plusDays(k)
            Repeat.WEEKLY -> e.start.plusWeeks(k)
            Repeat.MONTHLY -> e.start.plusMonths(k)
            Repeat.YEARLY -> e.start.plusYears(k)
        }
        if (e.repeat == Repeat.NONE) {
            val o = Occurrence(e, e.start, e.end)
            if (!o.lastDay.isBefore(from) && !o.firstDay.isAfter(to)) out += o
            continue
        }
        val spanDays = ChronoUnit.DAYS.between(e.start.toLocalDate(), e.end.toLocalDate())
        val gap = ChronoUnit.DAYS.between(e.start.toLocalDate(), from) - spanDays - 1
        var k = if (gap <= 0) 0L else when (e.repeat) {
            Repeat.DAILY -> gap
            Repeat.WEEKLY -> gap / 7
            Repeat.MONTHLY -> gap / 31
            Repeat.YEARLY -> gap / 366
            Repeat.NONE -> 0
        }
        var guard = 0
        while (guard++ < 2000) {
            val s = at(k)
            if (s.toLocalDate().isAfter(to)) break
            val o = Occurrence(e, s, length.addTo(s))
            if (!o.lastDay.isBefore(from)) out += o
            k++
        }
    }
    if (holidays) {
        for (y in from.year..to.year) {
            polishHolidays(y).forEach { (date, name) ->
                if (!date.isBefore(from) && !date.isAfter(to)) {
                    val ev = Event(-date.toEpochDay() - 1, name, date.atStartOfDay(), date.atTime(23, 59), allDay = true, color = HOLIDAY_COLOR)
                    out += Occurrence(ev, ev.start, ev.end)
                }
            }
        }
    }
    return out.sortedWith(compareBy<Occurrence> { !it.event.allDay }.thenBy { it.start }.thenBy { it.event.title })
}

/** Exact length between two times, keeping month-length quirks out of it. */
private class Duration(start: LocalDateTime, end: LocalDateTime) {
    private val minutes = ChronoUnit.MINUTES.between(start, end).coerceAtLeast(0)
    fun addTo(t: LocalDateTime): LocalDateTime = t.plusMinutes(minutes)
}

private fun easter(year: Int): LocalDate {
    val a = year % 19
    val b = year / 100
    val c = year % 100
    val d = b / 4
    val e = b % 4
    val f = (b + 8) / 25
    val g = (b - f + 1) / 3
    val h = (19 * a + b - d - g + 15) % 30
    val i = c / 4
    val k = c % 4
    val l = (32 + 2 * e + 2 * i - h - k) % 7
    val m = (a + 11 * h + 22 * l) / 451
    val month = (h + l - 7 * m + 114) / 31
    val day = ((h + l - 7 * m + 114) % 31) + 1
    return LocalDate.of(year, month, day)
}

/** Public holidays in Poland (days off work). */
fun polishHolidays(year: Int): List<Pair<LocalDate, String>> {
    val e = easter(year)
    return buildList {
        add(LocalDate.of(year, 1, 1) to "Nowy Rok")
        add(LocalDate.of(year, 1, 6) to "Trzech Króli")
        add(e to "Wielkanoc")
        add(e.plusDays(1) to "Poniedziałek Wielkanocny")
        add(LocalDate.of(year, 5, 1) to "Święto Pracy")
        add(LocalDate.of(year, 5, 3) to "Święto Konstytucji 3 Maja")
        add(e.plusDays(49) to "Zielone Świątki")
        add(e.plusDays(60) to "Boże Ciało")
        add(LocalDate.of(year, 8, 15) to "Wniebowzięcie NMP")
        add(LocalDate.of(year, 11, 1) to "Wszystkich Świętych")
        add(LocalDate.of(year, 11, 11) to "Święto Niepodległości")
        if (year >= 2025) add(LocalDate.of(year, 12, 24) to "Wigilia")
        add(LocalDate.of(year, 12, 25) to "Boże Narodzenie")
        add(LocalDate.of(year, 12, 26) to "Drugi dzień Świąt")
    }
}

// ---- storage format ----

data class Settings(val holidays: Boolean = true)

data class AppData(val events: List<Event> = emptyList(), val settings: Settings = Settings())

fun AppData.toJson(): String = JSONObject().apply {
    put("events", JSONArray().also { a ->
        events.forEach { e ->
            a.put(JSONObject().apply {
                put("id", e.id); put("title", e.title); put("start", e.start.toString()); put("end", e.end.toString())
                put("allDay", e.allDay); put("color", e.color); put("loc", e.location); put("desc", e.description)
                put("repeat", e.repeat.name); e.reminder?.let { put("reminder", it) }
            })
        }
    })
    put("holidays", settings.holidays)
}.toString()

fun appDataFromJson(json: String): AppData {
    val o = JSONObject(json)
    val a = o.optJSONArray("events") ?: JSONArray()
    val events = (0 until a.length()).mapNotNull { i ->
        val e = a.getJSONObject(i)
        runCatching {
            Event(
                id = e.getLong("id"),
                title = e.optString("title"),
                start = LocalDateTime.parse(e.getString("start")),
                end = LocalDateTime.parse(e.getString("end")),
                allDay = e.optBoolean("allDay"),
                color = e.optInt("color", DEFAULT_COLOR),
                location = e.optString("loc"),
                description = e.optString("desc"),
                repeat = runCatching { Repeat.valueOf(e.optString("repeat")) }.getOrDefault(Repeat.NONE),
                reminder = if (e.has("reminder")) e.getInt("reminder") else null,
            )
        }.getOrNull()
    }
    return AppData(events, Settings(holidays = o.optBoolean("holidays", true)))
}

fun sampleData(): AppData {
    val today = LocalDate.now()
    val mon = weekStart(today)
    return AppData(
        events = listOf(
            Event(1, "Spotkanie zespołu", mon.atTime(9, 30), mon.atTime(10, 30), color = 7, location = "Sala 2", repeat = Repeat.WEEKLY, reminder = 10),
            Event(2, "Siłownia", today.atTime(18, 0), today.atTime(19, 30), color = 4, repeat = Repeat.NONE),
            Event(3, "Obiad z rodziną", today.plusDays(2).atTime(14, 0), today.plusDays(2).atTime(16, 0), color = 2, location = "U babci"),
            Event(4, "Wyjazd w góry", today.plusDays(8).atStartOfDay(), today.plusDays(10).atTime(23, 59), allDay = true, color = 5),
            Event(5, "Dentysta", today.plusDays(4).atTime(11, 0), today.plusDays(4).atTime(11, 45), color = 0, reminder = 60),
        ),
    )
}

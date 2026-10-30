package com.tymo.zdrowydzien

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class Habit(val id: Long, val name: String, val emoji: String)

enum class MealType(val label: String, val emoji: String) {
    BREAKFAST("Śniadanie", "🍳"),
    LUNCH("Obiad", "🍲"),
    DINNER("Kolacja", "🥗"),
    SNACK("Przekąska", "🍎"),
}

data class Meal(val id: Long, val date: String, val name: String, val kcal: Int, val type: MealType)

data class Settings(
    val kcalGoal: Int = 2000,
    val waterGoal: Int = 2000,
    val glassMl: Int = 250,
    val reminders: Boolean = false,
)

data class AppData(
    val habits: List<Habit> = emptyList(),
    /** Date ("yyyy-MM-dd") -> ids of habits done that day. */
    val checks: Map<String, Set<Long>> = emptyMap(),
    val meals: List<Meal> = emptyList(),
    /** Date -> ml of water drunk that day. */
    val water: Map<String, Int> = emptyMap(),
    val settings: Settings = Settings(),
) {
    fun isDone(habitId: Long, date: String) = checks[date]?.contains(habitId) == true
    fun waterOn(date: String) = water[date] ?: 0
    fun mealsOn(date: String) = meals.filter { it.date == date }
    fun kcalOn(date: String) = mealsOn(date).sumOf { it.kcal }
    fun habitsDoneOn(date: String) = habits.count { isDone(it.id, date) }

    /** Days in a row the habit was done, counting back from today (or yesterday if today is not done yet). */
    fun streak(habitId: Long, today: LocalDate = LocalDate.now()): Int {
        var day = if (isDone(habitId, today.toString())) today else today.minusDays(1)
        var count = 0
        while (isDone(habitId, day.toString())) {
            count++
            day = day.minusDays(1)
        }
        return count
    }

    fun toJson(): String = JSONObject().apply {
        put("habits", JSONArray().also { a ->
            habits.forEach { a.put(JSONObject().put("id", it.id).put("name", it.name).put("emoji", it.emoji)) }
        })
        put("checks", JSONObject().also { o ->
            checks.forEach { (date, ids) -> o.put(date, JSONArray(ids.toList())) }
        })
        put("meals", JSONArray().also { a ->
            meals.forEach {
                a.put(
                    JSONObject().put("id", it.id).put("date", it.date).put("name", it.name)
                        .put("kcal", it.kcal).put("type", it.type.name)
                )
            }
        })
        put("water", JSONObject().also { o -> water.forEach { (d, ml) -> o.put(d, ml) } })
        put("settings", JSONObject().apply {
            put("kcalGoal", settings.kcalGoal)
            put("waterGoal", settings.waterGoal)
            put("glassMl", settings.glassMl)
            put("reminders", settings.reminders)
        })
    }.toString()

    companion object {
        fun fromJson(json: String): AppData {
            val o = JSONObject(json)
            val habitsArr = o.optJSONArray("habits") ?: JSONArray()
            val habits = (0 until habitsArr.length()).map {
                val h = habitsArr.getJSONObject(it)
                Habit(h.getLong("id"), h.getString("name"), h.optString("emoji", "✅"))
            }
            val checksObj = o.optJSONObject("checks") ?: JSONObject()
            val checks = checksObj.keys().asSequence().associateWith { date ->
                val arr = checksObj.getJSONArray(date)
                (0 until arr.length()).map { arr.getLong(it) }.toSet()
            }
            val mealsArr = o.optJSONArray("meals") ?: JSONArray()
            val meals = (0 until mealsArr.length()).map {
                val m = mealsArr.getJSONObject(it)
                Meal(
                    m.getLong("id"), m.getString("date"), m.getString("name"), m.getInt("kcal"),
                    runCatching { MealType.valueOf(m.getString("type")) }.getOrDefault(MealType.SNACK),
                )
            }
            val waterObj = o.optJSONObject("water") ?: JSONObject()
            val water = waterObj.keys().asSequence().associateWith { waterObj.getInt(it) }
            val s = o.optJSONObject("settings") ?: JSONObject()
            val settings = Settings(
                kcalGoal = s.optInt("kcalGoal", 2000),
                waterGoal = s.optInt("waterGoal", 2000),
                glassMl = s.optInt("glassMl", 250),
                reminders = s.optBoolean("reminders", false),
            )
            return AppData(habits, checks, meals, water, settings)
        }

        fun sample(): AppData = AppData(
            habits = listOf(
                Habit(1, "Spacer 30 minut", "🚶"),
                Habit(2, "Czytanie 20 stron", "📚"),
                Habit(3, "Bez słodyczy", "🍬"),
                Habit(4, "Sen przed 23:00", "😴"),
            ),
        )
    }
}

/** Quick-add products with typical calories per portion. */
val quickFoods = listOf(
    "Jabłko" to 80,
    "Banan" to 105,
    "Jogurt naturalny" to 120,
    "Kanapka z serem" to 250,
    "Owsianka" to 300,
    "Jajecznica (2 jajka)" to 220,
    "Kurczak z ryżem" to 550,
    "Pizza (2 kawałki)" to 570,
    "Kawa z mlekiem" to 60,
    "Batonik" to 230,
)

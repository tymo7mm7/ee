package com.tymo.nagrody

import org.json.JSONArray
import org.json.JSONObject

enum class GoalType(val label: String) {
    MONEY("Pieniądze"),
    TIME("Czas"),
    BOTH("Oba"),
}

enum class Kind { MONEY, TIME }

data class Reward(
    val id: Long,
    val name: String,
    val emoji: String = "🎁",
    val goalType: GoalType = GoalType.MONEY,
    val priceGr: Long = 0,        // money goal in grosze
    val goalMinutes: Int = 0,     // time goal
    val createdAt: Long = System.currentTimeMillis(),
    val claimedAt: Long? = null,
) {
    val needsMoney get() = goalType != GoalType.TIME && priceGr > 0
    val needsTime get() = goalType != GoalType.MONEY && goalMinutes > 0
}

data class Contribution(
    val id: Long,
    val rewardId: Long,
    val kind: Kind,
    val amount: Long,             // grosze or minutes
    val note: String = "",
    val at: Long = System.currentTimeMillis(),
)

/** Running stopwatch, kept in storage so it survives closing the app. */
data class TimerState(val rewardId: Long, val startedAt: Long, val label: String)

data class AppData(
    val rewards: List<Reward> = emptyList(),
    val contributions: List<Contribution> = emptyList(),
    val timer: TimerState? = null,
) {
    val active get() = rewards.filter { it.claimedAt == null }
    val claimed get() = rewards.filter { it.claimedAt != null }.sortedByDescending { it.claimedAt }

    fun savedGr(id: Long) = contributions.filter { it.rewardId == id && it.kind == Kind.MONEY }.sumOf { it.amount }
    fun minutes(id: Long) = contributions.filter { it.rewardId == id && it.kind == Kind.TIME }.sumOf { it.amount }.toInt()
    fun history(id: Long) = contributions.filter { it.rewardId == id }.sortedByDescending { it.at }

    fun moneyProgress(r: Reward) = if (!r.needsMoney) 1f else (savedGr(r.id).toFloat() / r.priceGr).coerceIn(0f, 1f)
    fun timeProgress(r: Reward) = if (!r.needsTime) 1f else (minutes(r.id).toFloat() / r.goalMinutes).coerceIn(0f, 1f)

    /** Overall progress: every goal of the reward must be reached. */
    fun progress(r: Reward): Float = when {
        r.needsMoney && r.needsTime -> (moneyProgress(r) + timeProgress(r)) / 2f
        r.needsMoney -> moneyProgress(r)
        r.needsTime -> timeProgress(r)
        else -> 1f
    }

    fun isReady(r: Reward) = moneyProgress(r) >= 1f && timeProgress(r) >= 1f

    fun toJson(): String = JSONObject().apply {
        put("rewards", JSONArray().also { a ->
            rewards.forEach { r ->
                a.put(JSONObject().apply {
                    put("id", r.id); put("name", r.name); put("emoji", r.emoji)
                    put("goal", r.goalType.name); put("price", r.priceGr); put("minutes", r.goalMinutes)
                    put("created", r.createdAt); r.claimedAt?.let { put("claimed", it) }
                })
            }
        })
        put("contributions", JSONArray().also { a ->
            contributions.forEach { c ->
                a.put(JSONObject().apply {
                    put("id", c.id); put("reward", c.rewardId); put("kind", c.kind.name)
                    put("amount", c.amount); put("note", c.note); put("at", c.at)
                })
            }
        })
        timer?.let { t ->
            put("timer", JSONObject().put("reward", t.rewardId).put("start", t.startedAt).put("label", t.label))
        }
    }.toString()

    companion object {
        fun fromJson(json: String): AppData {
            val o = JSONObject(json)
            val ra = o.optJSONArray("rewards") ?: JSONArray()
            val rewards = (0 until ra.length()).map {
                val r = ra.getJSONObject(it)
                Reward(
                    id = r.getLong("id"),
                    name = r.getString("name"),
                    emoji = r.optString("emoji", "🎁"),
                    goalType = runCatching { GoalType.valueOf(r.getString("goal")) }.getOrDefault(GoalType.MONEY),
                    priceGr = r.optLong("price", 0),
                    goalMinutes = r.optInt("minutes", 0),
                    createdAt = r.optLong("created", 0),
                    claimedAt = if (r.has("claimed")) r.getLong("claimed") else null,
                )
            }
            val ca = o.optJSONArray("contributions") ?: JSONArray()
            val contributions = (0 until ca.length()).map {
                val c = ca.getJSONObject(it)
                Contribution(
                    id = c.getLong("id"),
                    rewardId = c.getLong("reward"),
                    kind = runCatching { Kind.valueOf(c.getString("kind")) }.getOrDefault(Kind.MONEY),
                    amount = c.getLong("amount"),
                    note = c.optString("note", ""),
                    at = c.optLong("at", 0),
                )
            }
            val t = o.optJSONObject("timer")?.let {
                TimerState(it.getLong("reward"), it.getLong("start"), it.optString("label", "Nauka"))
            }
            return AppData(rewards, contributions, t)
        }

        fun sample(): AppData {
            val now = System.currentTimeMillis()
            return AppData(
                rewards = listOf(
                    Reward(1, "Nowa książka", "📚", GoalType.MONEY, priceGr = 6000, createdAt = now),
                    Reward(2, "Gra na konsolę", "🎮", GoalType.BOTH, priceGr = 25000, goalMinutes = 20 * 60, createdAt = now - 1),
                    Reward(3, "Wieczór filmowy", "🎬", GoalType.TIME, goalMinutes = 5 * 60, createdAt = now - 2),
                ),
                contributions = listOf(
                    Contribution(10, 1, Kind.MONEY, 2000, "Kieszonkowe", now - 86_400_000),
                    Contribution(11, 2, Kind.MONEY, 5000, "Odłożone", now - 2 * 86_400_000),
                    Contribution(12, 2, Kind.TIME, 90, "Nauka", now - 86_400_000),
                    Contribution(13, 3, Kind.TIME, 120, "Nauka", now - 3_600_000),
                ),
            )
        }
    }
}

fun formatMoney(gr: Long): String {
    val zl = gr / 100
    val g = gr % 100
    return if (g == 0L) "$zl zł" else "$zl,${"%02d".format(g)} zł"
}

fun formatMinutes(min: Int): String {
    val h = min / 60
    val m = min % 60
    return when {
        h == 0 -> "$m min"
        m == 0 -> "$h h"
        else -> "$h h $m min"
    }
}

/** Parses "12", "12,5" or "12.50" into grosze. */
fun parseMoney(text: String): Long? {
    val v = text.trim().replace(',', '.').toDoubleOrNull() ?: return null
    if (v <= 0 || v > 10_000_000) return null
    return Math.round(v * 100)
}

val emojiChoices = listOf("🎁", "📚", "🎮", "🎬", "🎧", "📱", "👟", "👕", "🍕", "🍰", "✈️", "🎢", "🎨", "⚽", "🎸", "💄", "🧸", "💻")

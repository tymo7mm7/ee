package com.tymo.zadania

import org.json.JSONArray
import org.json.JSONObject

enum class Priority(val label: String) {
    LOW("Niski"),
    MEDIUM("Średni"),
    HIGH("Wysoki"),
}

data class Task(
    val id: Long,
    val title: String,
    val note: String = "",
    val priority: Priority = Priority.MEDIUM,
    val dueDate: Long? = null,
    val done: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("note", note)
        put("priority", priority.name)
        if (dueDate != null) put("dueDate", dueDate)
        put("done", done)
        put("createdAt", createdAt)
    }

    companion object {
        fun fromJson(o: JSONObject) = Task(
            id = o.getLong("id"),
            title = o.getString("title"),
            note = o.optString("note", ""),
            priority = runCatching { Priority.valueOf(o.getString("priority")) }
                .getOrDefault(Priority.MEDIUM),
            dueDate = if (o.has("dueDate")) o.getLong("dueDate") else null,
            done = o.optBoolean("done", false),
            createdAt = o.optLong("createdAt", System.currentTimeMillis()),
        )
    }
}

fun List<Task>.toJsonString(): String =
    JSONArray().also { arr -> forEach { arr.put(it.toJson()) } }.toString()

fun parseTasks(json: String): List<Task> {
    val arr = JSONArray(json)
    return (0 until arr.length()).map { Task.fromJson(arr.getJSONObject(it)) }
}

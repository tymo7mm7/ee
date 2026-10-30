package com.tymo.zadania

import android.content.Context

/** Saves tasks on the phone (SharedPreferences, as JSON). */
class TaskRepository(context: Context) {
    private val prefs = context.getSharedPreferences("zadania", Context.MODE_PRIVATE)

    fun load(): List<Task>? {
        val json = prefs.getString(KEY, null) ?: return null
        return runCatching { parseTasks(json) }.getOrNull()
    }

    fun save(tasks: List<Task>) {
        prefs.edit().putString(KEY, tasks.toJsonString()).apply()
    }

    private companion object {
        const val KEY = "tasks"
    }
}

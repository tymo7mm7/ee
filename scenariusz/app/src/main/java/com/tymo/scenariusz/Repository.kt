package com.tymo.scenariusz

import android.content.Context

class Repository(context: Context) {
    private val prefs = context.getSharedPreferences("scenariusz", Context.MODE_PRIVATE)

    fun load(): List<Project>? = prefs.getString(KEY, null)?.let { runCatching { projectsFromJson(it) }.getOrNull() }

    fun save(projects: List<Project>) {
        prefs.edit().putString(KEY, projects.toJson()).apply()
    }

    private companion object {
        const val KEY = "projects"
    }
}

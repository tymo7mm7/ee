package com.tymo.oceny

import android.content.Context

class Repository(context: Context) {
    private val prefs = context.getSharedPreferences("oceny", Context.MODE_PRIVATE)

    fun load(): List<AppEntry>? = prefs.getString(KEY, null)?.let { runCatching { appsFromJson(it) }.getOrNull() }

    fun save(apps: List<AppEntry>) {
        prefs.edit().putString(KEY, apps.toJson()).apply()
    }

    private companion object {
        const val KEY = "apps"
    }
}

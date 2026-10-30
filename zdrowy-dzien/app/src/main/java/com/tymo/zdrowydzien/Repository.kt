package com.tymo.zdrowydzien

import android.content.Context

/** Keeps all app data on the phone as one JSON blob. */
class Repository(context: Context) {
    private val prefs = context.getSharedPreferences("zdrowy_dzien", Context.MODE_PRIVATE)

    fun load(): AppData? {
        val json = prefs.getString(KEY, null) ?: return null
        return runCatching { AppData.fromJson(json) }.getOrNull()
    }

    fun save(data: AppData) {
        prefs.edit().putString(KEY, data.toJson()).apply()
    }

    private companion object {
        const val KEY = "data"
    }
}

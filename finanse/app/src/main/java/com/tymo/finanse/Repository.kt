package com.tymo.finanse

import android.content.Context

class Repository(context: Context) {
    private val prefs = context.getSharedPreferences("finanse", Context.MODE_PRIVATE)

    fun load(): AppData? = prefs.getString(KEY, null)?.let { runCatching { AppData.fromJson(it) }.getOrNull() }

    fun save(data: AppData) {
        prefs.edit().putString(KEY, data.toJson()).apply()
    }

    private companion object {
        const val KEY = "data"
    }
}

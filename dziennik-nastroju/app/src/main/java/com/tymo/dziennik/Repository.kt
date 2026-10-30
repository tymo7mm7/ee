package com.tymo.dziennik

import android.content.Context

/** Keeps the journal on the phone only (backup to the cloud is disabled in the manifest). */
class Repository(context: Context) {
    private val prefs = context.getSharedPreferences("dziennik", Context.MODE_PRIVATE)

    fun load(): JournalData = prefs.getString(KEY, null)
        ?.let { runCatching { journalFromJson(it) }.getOrNull() }
        ?: JournalData()

    fun save(data: JournalData) {
        prefs.edit().putString(KEY, data.toJson()).apply()
    }

    private companion object {
        const val KEY = "data"
    }
}

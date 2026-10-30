package com.tymo.dziennik

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class JournalViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(app)
    private val _data = MutableStateFlow(repo.load())
    val data: StateFlow<JournalData> = _data.asStateFlow()

    /** Unlocked when no PIN is set; otherwise after entering the right PIN. */
    private val _unlocked = MutableStateFlow(_data.value.prefs.pinHash == null)
    val unlocked: StateFlow<Boolean> = _unlocked.asStateFlow()

    private fun update(change: (JournalData) -> JournalData) {
        _data.value = change(_data.value)
        repo.save(_data.value)
    }

    fun saveEntry(e: Entry) = update { it.copy(entries = it.entries + (e.date to e.copy(updatedAt = System.currentTimeMillis()))) }
    fun deleteEntry(date: String) = update { it.copy(entries = it.entries - date) }

    fun addCustomSymptom(name: String) = update {
        it.copy(prefs = it.prefs.copy(customSymptoms = (it.prefs.customSymptoms + name).distinct()))
    }

    fun tryUnlock(pin: String): Boolean {
        val ok = sha256(pin) == _data.value.prefs.pinHash
        if (ok) _unlocked.value = true
        return ok
    }

    fun setPin(pin: String?) = update { it.copy(prefs = it.prefs.copy(pinHash = pin?.let(::sha256))) }

    fun setReminder(enabled: Boolean, hour: Int) {
        update { it.copy(prefs = it.prefs.copy(reminder = enabled, reminderHour = hour)) }
        ReminderWorker.schedule(getApplication(), enabled, hour)
    }
}

package com.tymo.kalendarz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(app)
    private val _data = MutableStateFlow(repo.load() ?: sampleData().also { repo.save(it) })
    val data: StateFlow<AppData> = _data.asStateFlow()

    init {
        Reminders.scheduleAll(app)
    }

    private fun update(change: (AppData) -> AppData) {
        _data.value = change(_data.value)
        repo.save(_data.value)
        Reminders.scheduleAll(getApplication())
    }

    fun saveEvent(e: Event) = update { d ->
        val ev = if (e.id == 0L) e.copy(id = maxOf(System.currentTimeMillis(), (d.events.maxOfOrNull { it.id } ?: 0) + 1)) else e
        if (d.events.any { it.id == ev.id }) d.copy(events = d.events.map { if (it.id == ev.id) ev else it })
        else d.copy(events = d.events + ev)
    }

    fun deleteEvent(id: Long) = update { d -> d.copy(events = d.events.filterNot { it.id == id }) }

    fun setHolidays(on: Boolean) = update { it.copy(settings = it.settings.copy(holidays = on)) }
}

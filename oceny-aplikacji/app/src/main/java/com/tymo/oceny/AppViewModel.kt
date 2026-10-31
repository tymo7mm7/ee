package com.tymo.oceny

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(app)
    private val _apps = MutableStateFlow(repo.load() ?: startingApps().also { repo.save(it) })
    val apps: StateFlow<List<AppEntry>> = _apps.asStateFlow()

    private var lastId = System.currentTimeMillis()
    fun newId(): Long {
        lastId = maxOf(lastId + 1, System.currentTimeMillis())
        return lastId
    }

    private fun update(change: (List<AppEntry>) -> List<AppEntry>) {
        _apps.value = change(_apps.value)
        repo.save(_apps.value)
    }

    private fun updateApp(id: Long, change: (AppEntry) -> AppEntry) = update { list ->
        list.map { if (it.id == id) change(it).copy(updatedAt = System.currentTimeMillis()) else it }
    }

    fun saveApp(a: AppEntry) = update { list ->
        if (list.any { it.id == a.id }) list.map { if (it.id == a.id) a.copy(notes = it.notes, updatedAt = System.currentTimeMillis()) else it }
        else list + a
    }

    fun deleteApp(id: Long) = update { it.filterNot { a -> a.id == id } }
    fun setRating(id: Long, r: Int) = updateApp(id) { it.copy(rating = r) }
    fun setStatus(id: Long, s: AppStatus) = updateApp(id) { it.copy(status = s) }

    fun addNote(appId: Long, kind: Kind, text: String) = updateApp(appId) { it.copy(notes = it.notes + Note(newId(), kind, text.trim())) }
    fun saveNote(appId: Long, n: Note) = updateApp(appId) { a -> a.copy(notes = a.notes.map { if (it.id == n.id) n else it }) }
    fun deleteNote(appId: Long, noteId: Long) = updateApp(appId) { a -> a.copy(notes = a.notes.filterNot { it.id == noteId }) }
    fun toggleDone(appId: Long, noteId: Long) = updateApp(appId) { a ->
        a.copy(notes = a.notes.map { if (it.id == noteId) it.copy(done = !it.done) else it })
    }
}

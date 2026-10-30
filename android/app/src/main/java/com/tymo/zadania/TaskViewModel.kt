package com.tymo.zadania

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class Filter(val label: String) {
    ALL("Wszystkie"),
    ACTIVE("Do zrobienia"),
    DONE("Ukończone"),
}

data class UiState(
    val tasks: List<Task> = emptyList(),
    val filter: Filter = Filter.ALL,
    val query: String = "",
) {
    val visible: List<Task>
        get() = tasks
            .filter {
                when (filter) {
                    Filter.ALL -> true
                    Filter.ACTIVE -> !it.done
                    Filter.DONE -> it.done
                }
            }
            .filter {
                query.isBlank() ||
                    it.title.contains(query, ignoreCase = true) ||
                    it.note.contains(query, ignoreCase = true)
            }
            .sortedWith(
                compareBy<Task> { it.done }
                    .thenByDescending { it.priority.ordinal }
                    .thenBy { it.dueDate ?: Long.MAX_VALUE }
                    .thenByDescending { it.createdAt }
            )

    val doneCount get() = tasks.count { it.done }
}

class TaskViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = TaskRepository(app)
    private val _state = MutableStateFlow(UiState(tasks = repo.load() ?: sampleTasks()))
    val state: StateFlow<UiState> = _state.asStateFlow()

    private fun updateTasks(change: (List<Task>) -> List<Task>) {
        _state.update { it.copy(tasks = change(it.tasks)) }
        repo.save(_state.value.tasks)
    }

    fun setFilter(f: Filter) = _state.update { it.copy(filter = f) }
    fun setQuery(q: String) = _state.update { it.copy(query = q) }

    fun save(task: Task) = updateTasks { list ->
        if (list.any { it.id == task.id }) list.map { if (it.id == task.id) task else it }
        else list + task
    }

    fun toggle(task: Task) = save(task.copy(done = !task.done))

    fun delete(task: Task) = updateTasks { list -> list.filterNot { it.id == task.id } }

    fun clearDone() = updateTasks { list -> list.filterNot { it.done } }

    private fun sampleTasks(): List<Task> {
        val now = System.currentTimeMillis()
        val day = 24L * 60 * 60 * 1000
        return listOf(
            Task(1, "Witaj w aplikacji Zadania 👋", "Dotknij zadania, aby je edytować.", Priority.HIGH, createdAt = now),
            Task(2, "Zrobić zakupy", "Mleko, chleb, jajka", Priority.MEDIUM, now + day, createdAt = now - 1),
            Task(3, "Oddać projekt", "", Priority.HIGH, now + 3 * day, createdAt = now - 2),
            Task(4, "Zainstalować aplikację", "", Priority.LOW, done = true, createdAt = now - 3),
        ).also { repo.save(it) }
    }
}

package com.tymo.zdrowydzien

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(app)
    private val _data = MutableStateFlow(repo.load() ?: AppData.sample().also { repo.save(it) })
    val data: StateFlow<AppData> = _data.asStateFlow()

    private fun today() = LocalDate.now().toString()

    private fun update(change: (AppData) -> AppData) {
        _data.value = change(_data.value)
        repo.save(_data.value)
    }

    // Habits
    fun toggleHabit(id: Long, date: String = today()) = update { d ->
        val set = d.checks[date].orEmpty()
        val newSet = if (id in set) set - id else set + id
        d.copy(checks = d.checks + (date to newSet))
    }

    fun addHabit(name: String, emoji: String) = update { d ->
        d.copy(habits = d.habits + Habit(System.currentTimeMillis(), name, emoji.ifBlank { "✅" }))
    }

    fun deleteHabit(id: Long) = update { d ->
        d.copy(
            habits = d.habits.filterNot { it.id == id },
            checks = d.checks.mapValues { it.value - id },
        )
    }

    // Water
    fun addWater(ml: Int) = update { d ->
        val date = today()
        d.copy(water = d.water + (date to (d.waterOn(date) + ml).coerceAtLeast(0)))
    }

    // Meals
    fun addMeal(name: String, kcal: Int, type: MealType) = update { d ->
        d.copy(meals = d.meals + Meal(System.currentTimeMillis(), today(), name, kcal, type))
    }

    fun deleteMeal(id: Long) = update { d -> d.copy(meals = d.meals.filterNot { it.id == id }) }

    // Settings
    fun saveSettings(s: Settings) {
        update { it.copy(settings = s) }
        WaterReminderWorker.schedule(getApplication(), s.reminders)
    }
}

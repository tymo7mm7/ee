package com.tymo.finanse

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(app)
    private val _data = MutableStateFlow((repo.load() ?: AppData.sample()).applyRecurring().also { repo.save(it) })
    val data: StateFlow<AppData> = _data.asStateFlow()

    private fun update(change: (AppData) -> AppData) {
        _data.value = change(_data.value)
        repo.save(_data.value)
    }

    /** Called when the app comes back to the foreground, e.g. on a new day. */
    fun refreshRecurring() = update { it.applyRecurring() }

    private fun newId(d: AppData) =
        maxOf(System.currentTimeMillis(), (d.txs.maxOfOrNull { it.id } ?: 0L) + 1)

    /** Saves a transaction; id 0 means a new one. */
    fun saveTx(tx: Tx) = update { d ->
        val t = if (tx.id == 0L) tx.copy(id = newId(d)) else tx
        if (d.txs.any { it.id == t.id }) d.copy(txs = d.txs.map { if (it.id == t.id) t else it })
        else d.copy(txs = d.txs + t)
    }

    fun deleteTx(id: Long) = update { d -> d.copy(txs = d.txs.filterNot { it.id == id }) }

    fun setBudget(key: String, gr: Long?) = update { d ->
        d.copy(budgets = if (gr == null) d.budgets - key else d.budgets + (key to gr))
    }

    fun saveRecurring(rec: Recurring) = update { d ->
        val r = if (rec.id == 0L) rec.copy(id = System.currentTimeMillis()) else rec
        val list = if (d.recurring.any { it.id == r.id }) d.recurring.map { if (it.id == r.id) r else it } else d.recurring + r
        d.copy(recurring = list).applyRecurring()
    }

    fun deleteRecurring(id: Long) = update { d -> d.copy(recurring = d.recurring.filterNot { it.id == id }) }
}

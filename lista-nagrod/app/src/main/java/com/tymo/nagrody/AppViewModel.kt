package com.tymo.nagrody

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(app)
    private val _data = MutableStateFlow(repo.load() ?: AppData.sample().also { repo.save(it) })
    val data: StateFlow<AppData> = _data.asStateFlow()

    /** Set when a contribution completes a reward, to show a celebration. */
    private val _celebrate = MutableStateFlow<Reward?>(null)
    val celebrate: StateFlow<Reward?> = _celebrate.asStateFlow()

    private fun update(change: (AppData) -> AppData) {
        _data.value = change(_data.value)
        repo.save(_data.value)
    }

    fun saveReward(r: Reward) = update { d ->
        if (d.rewards.any { it.id == r.id }) d.copy(rewards = d.rewards.map { if (it.id == r.id) r else it })
        else d.copy(rewards = d.rewards + r)
    }

    fun deleteReward(id: Long) = update { d ->
        d.copy(
            rewards = d.rewards.filterNot { it.id == id },
            contributions = d.contributions.filterNot { it.rewardId == id },
            timer = d.timer?.takeIf { it.rewardId != id },
        )
    }

    fun contribute(rewardId: Long, kind: Kind, amount: Long, note: String = "") {
        if (amount <= 0) return
        val wasReady = _data.value.rewards.find { it.id == rewardId }?.let { _data.value.isReady(it) } ?: false
        update { d -> d.copy(contributions = d.contributions + Contribution(System.nanoTime(), rewardId, kind, amount, note)) }
        val reward = _data.value.rewards.find { it.id == rewardId } ?: return
        if (!wasReady && _data.value.isReady(reward)) _celebrate.value = reward
    }

    fun deleteContribution(id: Long) = update { d -> d.copy(contributions = d.contributions.filterNot { it.id == id }) }

    fun dismissCelebration() {
        _celebrate.value = null
    }

    fun claim(id: Long) = update { d ->
        d.copy(rewards = d.rewards.map { if (it.id == id) it.copy(claimedAt = System.currentTimeMillis()) else it })
    }

    fun unclaim(id: Long) = update { d ->
        d.copy(rewards = d.rewards.map { if (it.id == id) it.copy(claimedAt = null) else it })
    }

    fun startTimer(rewardId: Long, label: String) = update {
        it.copy(timer = TimerState(rewardId, System.currentTimeMillis(), label))
    }

    /** Stops the stopwatch and books the time (at least 1 minute) to the reward. */
    fun stopTimer(save: Boolean = true) {
        val t = _data.value.timer ?: return
        update { it.copy(timer = null) }
        val minutes = ((System.currentTimeMillis() - t.startedAt) / 60_000).coerceAtLeast(1)
        if (save) contribute(t.rewardId, Kind.TIME, minutes, t.label)
    }
}

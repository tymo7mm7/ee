@file:OptIn(ExperimentalMaterial3Api::class)

package com.tymo.nagrody.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tymo.nagrody.AppViewModel
import com.tymo.nagrody.Kind
import com.tymo.nagrody.Reward

private enum class Tab(val label: String, val icon: ImageVector) {
    REWARDS("Nagrody", Icons.Default.Home),
    TIMER("Stoper", Icons.Default.PlayArrow),
    CLAIMED("Zdobyte", Icons.Default.Star),
}

/** Which "add progress" dialog is open. */
data class ContributeRequest(val reward: Reward, val kind: Kind)

@Composable
fun AppRoot(vm: AppViewModel = viewModel()) {
    val data by vm.data.collectAsStateWithLifecycle()
    val celebrate by vm.celebrate.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var detailId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editing by remember { mutableStateOf<Reward?>(null) }
    var contributing by remember { mutableStateOf<ContributeRequest?>(null) }

    val detail = detailId?.let { id -> data.rewards.find { it.id == id } }
    if (detail != null) {
        RewardDetail(
            reward = detail,
            data = data,
            onBack = { detailId = null },
            onEdit = { editing = detail },
            onDelete = { vm.deleteReward(detail.id); detailId = null },
            onContribute = { kind -> contributing = ContributeRequest(detail, kind) },
            onDeleteContribution = vm::deleteContribution,
            onClaim = { vm.claim(detail.id) },
            onUnclaim = { vm.unclaim(detail.id) },
        )
    } else {
        val current = Tab.entries[tab]
        Scaffold(
            topBar = {
                TopAppBar(title = {
                    Text(if (current == Tab.REWARDS) "Lista Nagród 🎁" else current.label, fontWeight = FontWeight.Bold)
                })
            },
            bottomBar = {
                NavigationBar {
                    Tab.entries.forEachIndexed { i, t ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i },
                            icon = { Icon(t.icon, null) },
                            label = { Text(t.label) },
                        )
                    }
                }
            },
            floatingActionButton = {
                if (current == Tab.REWARDS) {
                    ExtendedFloatingActionButton(
                        onClick = { editing = Reward(id = System.currentTimeMillis(), name = "") },
                        icon = { Icon(Icons.Default.Add, null) },
                        text = { Text("Nowa nagroda") },
                    )
                }
            },
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when (current) {
                    Tab.REWARDS -> RewardsScreen(
                        data = data,
                        onOpen = { detailId = it.id },
                        onContribute = { r, k -> contributing = ContributeRequest(r, k) },
                        onClaim = { vm.claim(it.id) },
                    )
                    Tab.TIMER -> TimerScreen(data, vm::startTimer, { vm.stopTimer(true) }, { vm.stopTimer(false) })
                    Tab.CLAIMED -> ClaimedScreen(data, onOpen = { detailId = it.id })
                }
            }
        }
    }

    editing?.let { r ->
        RewardEditor(
            initial = r,
            isNew = data.rewards.none { it.id == r.id },
            onDismiss = { editing = null },
            onSave = { vm.saveReward(it); editing = null },
        )
    }

    contributing?.let { req ->
        ContributeDialog(req.reward, req.kind, onDismiss = { contributing = null }) { amount, note ->
            vm.contribute(req.reward.id, req.kind, amount, note)
            contributing = null
        }
    }

    celebrate?.let { r ->
        CelebrationDialog(
            reward = r,
            onClaim = { vm.claim(r.id); vm.dismissCelebration() },
            onLater = vm::dismissCelebration,
        )
    }
}

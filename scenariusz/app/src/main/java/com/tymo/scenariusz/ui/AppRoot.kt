package com.tymo.scenariusz.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tymo.scenariusz.AppViewModel
import com.tymo.scenariusz.Scene

/** Simple navigation: project list → project → scene editor. */
@Composable
fun AppRoot(vm: AppViewModel = viewModel()) {
    val projects by vm.projects.collectAsStateWithLifecycle()
    var projectId by rememberSaveable { mutableStateOf<Long?>(null) }
    var sceneId by rememberSaveable { mutableStateOf<Long?>(null) }
    var newScene by rememberSaveable { mutableStateOf(false) }

    val project = projectId?.let { id -> projects.find { it.id == id } }
    if (project == null) {
        ProjectsScreen(projects, vm, onOpen = { projectId = it })
        return
    }

    val scene = when {
        newScene -> Scene(id = -1)
        else -> sceneId?.let { id -> project.scenes.find { it.id == id } }
    }
    if (scene != null) {
        SceneEditor(
            project = project,
            initial = scene,
            number = if (newScene) project.scenes.size + 1 else project.scenes.indexOfFirst { it.id == scene.id } + 1,
            newId = vm::newId,
            onClose = { sceneId = null; newScene = false },
            onSave = { s ->
                vm.saveScene(project.id, if (s.id == -1L) s.copy(id = vm.newId()) else s)
                sceneId = null; newScene = false
            },
            onDelete = { vm.deleteScene(project.id, scene.id); sceneId = null; newScene = false },
            onDuplicate = { vm.duplicateScene(project.id, scene.id); sceneId = null },
            onAddCharacter = { vm.saveCharacter(project.id, it) },
        )
        return
    }

    ProjectScreen(
        project = project,
        vm = vm,
        onBack = { projectId = null },
        onOpenScene = { sceneId = it },
        onNewScene = { newScene = true },
    )
}

package com.tymo.scenariusz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(app)
    private val _projects = MutableStateFlow(repo.load() ?: listOf(sampleProject()).also { repo.save(it) })
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private var lastId = System.currentTimeMillis()
    fun newId(): Long {
        lastId = maxOf(lastId + 1, System.currentTimeMillis())
        return lastId
    }

    private fun update(change: (List<Project>) -> List<Project>) {
        _projects.value = change(_projects.value)
        repo.save(_projects.value)
    }

    private fun updateProject(id: Long, change: (Project) -> Project) = update { list ->
        list.map { if (it.id == id) change(it).copy(updatedAt = System.currentTimeMillis()) else it }
    }

    fun saveProject(p: Project) = update { list ->
        if (list.any { it.id == p.id }) list.map { if (it.id == p.id) p.copy(scenes = it.scenes, characters = it.characters, updatedAt = System.currentTimeMillis()) else it }
        else list + p
    }

    fun deleteProject(id: Long) = update { it.filterNot { p -> p.id == id } }

    fun saveScene(projectId: Long, s: Scene) = updateProject(projectId) { p ->
        // Everyone who speaks in the scene is also listed as present.
        val scene = s.copy(characterIds = s.characterIds + s.dialogue.map { it.characterId })
        if (p.scenes.any { it.id == s.id }) p.copy(scenes = p.scenes.map { if (it.id == s.id) scene else it })
        else p.copy(scenes = p.scenes + scene)
    }

    fun deleteScene(projectId: Long, sceneId: Long) = updateProject(projectId) { p -> p.copy(scenes = p.scenes.filterNot { it.id == sceneId }) }

    fun duplicateScene(projectId: Long, sceneId: Long) = updateProject(projectId) { p ->
        val i = p.scenes.indexOfFirst { it.id == sceneId }
        if (i < 0) return@updateProject p
        val src = p.scenes[i]
        val copy = src.copy(id = newId(), title = src.title + " (kopia)", status = Status.DRAFT, dialogue = src.dialogue.map { it.copy(id = newId()) })
        p.copy(scenes = p.scenes.toMutableList().apply { add(i + 1, copy) })
    }

    fun moveScene(projectId: Long, sceneId: Long, delta: Int) = updateProject(projectId) { p ->
        val i = p.scenes.indexOfFirst { it.id == sceneId }
        val j = i + delta
        if (i < 0 || j !in p.scenes.indices) return@updateProject p
        p.copy(scenes = p.scenes.toMutableList().apply { add(j, removeAt(i)) })
    }

    fun saveCharacter(projectId: Long, c: Character) = updateProject(projectId) { p ->
        if (p.characters.any { it.id == c.id }) p.copy(characters = p.characters.map { if (it.id == c.id) c else it })
        else p.copy(characters = p.characters + c)
    }

    fun deleteCharacter(projectId: Long, id: Long) = updateProject(projectId) { p ->
        p.copy(
            characters = p.characters.filterNot { it.id == id },
            scenes = p.scenes.map { s -> s.copy(characterIds = s.characterIds - id, dialogue = s.dialogue.filterNot { it.characterId == id }) },
        )
    }
}

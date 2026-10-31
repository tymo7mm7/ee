package com.tymo.scenariusz

import org.json.JSONArray
import org.json.JSONObject

enum class Place(val label: String) { INT("INT."), EXT("EXT."), INT_EXT("INT./EXT.") }

enum class TimeOfDay(val label: String) { DAY("DZIEŃ"), NIGHT("NOC"), DAWN("ŚWIT"), DUSK("ZMIERZCH"), CONT("CIĄGŁOŚĆ") }

enum class Status(val label: String, val emoji: String) {
    DRAFT("Szkic", "✏️"),
    READY("Gotowa", "✅"),
    SHOT("Nakręcona", "🎬"),
}

data class Character(val id: Long, val name: String, val description: String = "", val color: Int = 0)

data class Line(val id: Long, val characterId: Long, val parenthetical: String = "", val text: String = "")

data class Scene(
    val id: Long,
    val place: Place = Place.INT,
    val location: String = "",
    val time: TimeOfDay = TimeOfDay.DAY,
    val title: String = "",
    val description: String = "",
    val characterIds: Set<Long> = emptySet(),
    val dialogue: List<Line> = emptyList(),
    val minutes: Int = 2,
    val status: Status = Status.DRAFT,
    val notes: String = "",
) {
    val heading get() = "${place.label} ${location.ifBlank { "MIEJSCE" }.uppercase()} – ${time.label}"
}

data class Project(
    val id: Long,
    val title: String,
    val genre: String = "",
    val logline: String = "",
    val author: String = "",
    val scenes: List<Scene> = emptyList(),
    val characters: List<Character> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    fun character(id: Long) = characters.find { it.id == id }
    val totalMinutes get() = scenes.sumOf { it.minutes }
}

// ---- JSON ----

private fun Scene.toJson() = JSONObject().apply {
    put("id", id); put("place", place.name); put("loc", location); put("time", time.name)
    put("title", title); put("desc", description); put("chars", JSONArray(characterIds.toList()))
    put("min", minutes); put("status", status.name); put("notes", notes)
    put("dialogue", JSONArray().also { a ->
        dialogue.forEach { a.put(JSONObject().put("id", it.id).put("c", it.characterId).put("p", it.parenthetical).put("t", it.text)) }
    })
}

private inline fun <reified E : Enum<E>> enumOr(name: String?, default: E): E =
    runCatching { enumValueOf<E>(name ?: "") }.getOrDefault(default)

private fun sceneFrom(o: JSONObject): Scene {
    val ch = o.optJSONArray("chars") ?: JSONArray()
    val dl = o.optJSONArray("dialogue") ?: JSONArray()
    return Scene(
        id = o.getLong("id"),
        place = enumOr(o.optString("place"), Place.INT),
        location = o.optString("loc"),
        time = enumOr(o.optString("time"), TimeOfDay.DAY),
        title = o.optString("title"),
        description = o.optString("desc"),
        characterIds = (0 until ch.length()).map { ch.getLong(it) }.toSet(),
        dialogue = (0 until dl.length()).map {
            val l = dl.getJSONObject(it)
            Line(l.getLong("id"), l.getLong("c"), l.optString("p"), l.optString("t"))
        },
        minutes = o.optInt("min", 2),
        status = enumOr(o.optString("status"), Status.DRAFT),
        notes = o.optString("notes"),
    )
}

fun List<Project>.toJson(): String = JSONArray().also { arr ->
    forEach { p ->
        arr.put(JSONObject().apply {
            put("id", p.id); put("title", p.title); put("genre", p.genre); put("logline", p.logline)
            put("author", p.author); put("updated", p.updatedAt)
            put("scenes", JSONArray().also { a -> p.scenes.forEach { a.put(it.toJson()) } })
            put("characters", JSONArray().also { a ->
                p.characters.forEach { a.put(JSONObject().put("id", it.id).put("name", it.name).put("desc", it.description).put("color", it.color)) }
            })
        })
    }
}.toString()

fun projectsFromJson(json: String): List<Project> {
    val arr = JSONArray(json)
    return (0 until arr.length()).map { i ->
        val o = arr.getJSONObject(i)
        val sa = o.optJSONArray("scenes") ?: JSONArray()
        val ca = o.optJSONArray("characters") ?: JSONArray()
        Project(
            id = o.getLong("id"),
            title = o.optString("title"),
            genre = o.optString("genre"),
            logline = o.optString("logline"),
            author = o.optString("author"),
            updatedAt = o.optLong("updated"),
            scenes = (0 until sa.length()).map { sceneFrom(sa.getJSONObject(it)) },
            characters = (0 until ca.length()).map {
                val c = ca.getJSONObject(it)
                Character(c.getLong("id"), c.optString("name"), c.optString("desc"), c.optInt("color"))
            },
        )
    }
}

fun sampleProject(): Project {
    val anna = Character(1, "ANNA", "30 lat, konduktorka. Skrywa tajemnicę z przeszłości.", 0)
    val marek = Character(2, "MAREK", "Pasażer bez biletu, nerwowy, ciągle patrzy na zegarek.", 1)
    return Project(
        id = 1,
        title = "Ostatni pociąg",
        genre = "Thriller",
        logline = "Konduktorka nocnego pociągu odkrywa, że jeden z pasażerów wie o niej więcej, niż powinien.",
        author = "",
        characters = listOf(anna, marek),
        scenes = listOf(
            Scene(
                10, Place.EXT, "Peron dworca", TimeOfDay.NIGHT, "Odjazd",
                "Pusty peron oświetlony jedną migającą lampą. Deszcz. ANNA (30) sprawdza zegarek i daje sygnał do odjazdu. W ostatniej chwili do wagonu wskakuje MAREK.",
                setOf(1, 2), listOf(Line(100, 1, "", "Ostatnie wezwanie! Drzwi zamykają się!")), 2, Status.READY,
            ),
            Scene(
                11, Place.INT, "Wagon nr 7", TimeOfDay.NIGHT, "Bilet",
                "Wagon prawie pusty. Marek siada przy oknie, ociera twarz z deszczu. Anna podchodzi do niego.",
                setOf(1, 2),
                listOf(
                    Line(101, 1, "", "Bilet do kontroli, proszę."),
                    Line(102, 2, "uśmiecha się", "Nie mam biletu. Ale mam coś, co do pani należy."),
                    Line(103, 1, "zaskoczona", "Słucham?"),
                ),
                3, Status.DRAFT,
            ),
            Scene(
                12, Place.INT, "Przedział służbowy", TimeOfDay.NIGHT, "Fotografia",
                "Anna zamyka drzwi. Drżącymi rękami ogląda starą fotografię, którą dał jej Marek. Na zdjęciu ona – dziesięć lat młodsza – i ktoś, czyja twarz została wydrapana.",
                setOf(1), emptyList(), 2, Status.DRAFT,
            ),
        ),
    )
}

// ---- Screenplay text ----

sealed interface ScriptPart {
    data class Title(val text: String) : ScriptPart
    data class Info(val text: String) : ScriptPart
    data class Heading(val number: Int, val text: String) : ScriptPart
    data class Action(val text: String) : ScriptPart
    data class Speaker(val name: String) : ScriptPart
    data class Paren(val text: String) : ScriptPart
    data class Dialog(val text: String) : ScriptPart
}

fun Project.scriptParts(): List<ScriptPart> = buildList {
    add(ScriptPart.Title(title.uppercase()))
    listOf(genre, author.takeIf { it.isNotBlank() }?.let { "autor: $it" }).filterNotNull().filter { it.isNotBlank() }
        .takeIf { it.isNotEmpty() }?.let { add(ScriptPart.Info(it.joinToString(" · "))) }
    if (logline.isNotBlank()) add(ScriptPart.Info(logline))
    scenes.forEachIndexed { i, s ->
        add(ScriptPart.Heading(i + 1, s.heading))
        if (s.description.isNotBlank()) add(ScriptPart.Action(s.description))
        s.dialogue.forEach { l ->
            add(ScriptPart.Speaker(character(l.characterId)?.name?.uppercase() ?: "?"))
            if (l.parenthetical.isNotBlank()) add(ScriptPart.Paren("(${l.parenthetical.trim('(', ')')})"))
            if (l.text.isNotBlank()) add(ScriptPart.Dialog(l.text))
        }
    }
}

fun Project.toPlainText(): String = buildString {
    scriptParts().forEach { p ->
        when (p) {
            is ScriptPart.Title -> appendLine(p.text)
            is ScriptPart.Info -> appendLine(p.text)
            is ScriptPart.Heading -> { appendLine(); appendLine("SCENA ${p.number}. ${p.text}") }
            is ScriptPart.Action -> { appendLine(); appendLine(p.text) }
            is ScriptPart.Speaker -> { appendLine(); appendLine("                    ${p.name}") }
            is ScriptPart.Paren -> appendLine("               ${p.text}")
            is ScriptPart.Dialog -> appendLine("          ${p.text}")
        }
    }
}

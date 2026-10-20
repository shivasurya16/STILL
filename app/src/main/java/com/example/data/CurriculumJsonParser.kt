package com.example.data

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurriculumJsonParser {

    fun validateAndParse(rawJson: String): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        val seenIds = mutableSetOf<String>()

        val trimmed = rawJson.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(
                isValid = false,
                errors = listOf("JSON input cannot be empty.")
            )
        }

        val root = try {
            JSONObject(trimmed)
        } catch (e: Exception) {
            return ValidationResult(
                isValid = false,
                errors = listOf("Malformed JSON: ${e.localizedMessage ?: "Invalid JSON syntax"}")
            )
        }

        // 1. Version check
        if (!root.has("version")) {
            errors.add("Missing required root field: 'version'.")
        } else {
            val ver = root.optInt("version", -1)
            if (ver != 1) {
                errors.add("Unsupported version '$ver'. Only version 1 is supported.")
            }
        }

        // 2. Type check
        if (!root.has("type")) {
            errors.add("Missing required root field: 'type'.")
        }
        val type = root.optString("type", "").trim().lowercase()
        if (type != "academics" && type != "skills" && type != "still_backup") {
            errors.add("Unsupported type '$type'. Must be either 'academics' or 'skills'.")
        }

        if (errors.isNotEmpty()) {
            return ValidationResult(isValid = false, errors = errors, rawJson = rawJson)
        }

        // 3. Hierarchy parsing according to type
        when (type) {
            "academics" -> {
                if (!root.has("subjects")) {
                    errors.add("Missing required array: 'subjects'.")
                    return ValidationResult(isValid = false, errors = errors, rawJson = rawJson)
                }

                val subjectsArr = root.optJSONArray("subjects")
                if (subjectsArr == null) {
                    errors.add("'subjects' must be a valid JSON array.")
                    return ValidationResult(isValid = false, errors = errors, rawJson = rawJson)
                }

                val parsedSubjects = mutableListOf<Subject>()
                val summaryList = mutableListOf<String>()
                var totalChapters = 0
                var totalTopics = 0
                var totalSubtopics = 0
                var totalTrackable = 0

                for (sIdx in 0 until subjectsArr.length()) {
                    val sObj = subjectsArr.optJSONObject(sIdx)
                    if (sObj == null) {
                        errors.add("Subject at index $sIdx is not a valid JSON object.")
                        continue
                    }

                    val sId = sObj.optString("id", "").trim()
                    if (sId.isEmpty()) {
                        errors.add("Subject at index $sIdx has a missing or empty 'id'.")
                    } else if (!seenIds.add(sId)) {
                        errors.add("Duplicate ID '$sId' detected on Subject at index $sIdx. IDs must be globally unique.")
                    }

                    val sName = sObj.optString("name", "").trim()
                    if (sName.isEmpty()) {
                        errors.add("Subject '$sId' (index $sIdx) has a missing or empty 'name'.")
                    }

                    val sDesc = if (sObj.has("description")) sObj.optString("description") else null

                    if (!sObj.has("chapters")) {
                        errors.add("Subject '$sName' is missing required 'chapters' array.")
                        continue
                    }
                    val chaptersArr = sObj.optJSONArray("chapters")
                    if (chaptersArr == null) {
                        errors.add("Subject '$sName' has an invalid 'chapters' field (must be an array).")
                        continue
                    }

                    val parsedChapters = mutableListOf<Chapter>()

                    for (cIdx in 0 until chaptersArr.length()) {
                        val cObj = chaptersArr.optJSONObject(cIdx)
                        if (cObj == null) {
                            errors.add("Chapter at index $cIdx in Subject '$sName' is not a valid object.")
                            continue
                        }

                        val cId = cObj.optString("id", "").trim()
                        if (cId.isEmpty()) {
                            errors.add("Chapter at index $cIdx in Subject '$sName' has a missing or empty 'id'.")
                        } else if (!seenIds.add(cId)) {
                            errors.add("Duplicate ID '$cId' detected on Chapter in Subject '$sName'.")
                        }

                        val cName = cObj.optString("name", "").trim()
                        if (cName.isEmpty()) {
                            errors.add("Chapter '$cId' in Subject '$sName' has a missing or empty 'name'.")
                        }

                        val cDesc = if (cObj.has("description")) cObj.optString("description") else null

                        if (!cObj.has("topics")) {
                            errors.add("Chapter '$cName' is missing required 'topics' array.")
                            continue
                        }
                        val topicsArr = cObj.optJSONArray("topics")
                        if (topicsArr == null) {
                            errors.add("Chapter '$cName' has an invalid 'topics' field (must be an array).")
                            continue
                        }

                        val parsedTopics = mutableListOf<Topic>()

                        for (tIdx in 0 until topicsArr.length()) {
                            val tObj = topicsArr.optJSONObject(tIdx)
                            if (tObj == null) {
                                errors.add("Topic at index $tIdx in Chapter '$cName' is not a valid object.")
                                continue
                            }

                            val tId = tObj.optString("id", "").trim()
                            if (tId.isEmpty()) {
                                errors.add("Topic at index $tIdx in Chapter '$cName' has a missing or empty 'id'.")
                            } else if (!seenIds.add(tId)) {
                                errors.add("Duplicate ID '$tId' detected on Topic in Chapter '$cName'.")
                            }

                            val tName = tObj.optString("name", "").trim()
                            if (tName.isEmpty()) {
                                errors.add("Topic '$tId' in Chapter '$cName' has a missing or empty 'name'.")
                            }

                            val tDesc = if (tObj.has("description")) tObj.optString("description") else null

                            val parsedSubtopics = mutableListOf<Subtopic>()
                            if (tObj.has("subtopics")) {
                                val subArr = tObj.optJSONArray("subtopics")
                                if (subArr == null) {
                                    errors.add("Topic '$tName' has an invalid 'subtopics' field (must be an array).")
                                } else {
                                    for (stIdx in 0 until subArr.length()) {
                                        val stObj = subArr.optJSONObject(stIdx)
                                        if (stObj == null) {
                                            errors.add("Subtopic at index $stIdx in Topic '$tName' is not a valid object.")
                                            continue
                                        }
                                        val stId = stObj.optString("id", "").trim()
                                        if (stId.isEmpty()) {
                                            errors.add("Subtopic at index $stIdx in Topic '$tName' has missing or empty 'id'.")
                                        } else if (!seenIds.add(stId)) {
                                            errors.add("Duplicate ID '$stId' detected on Subtopic in Topic '$tName'.")
                                        }
                                        val stName = stObj.optString("name", "").trim()
                                        if (stName.isEmpty()) {
                                            errors.add("Subtopic '$stId' in Topic '$tName' has missing or empty 'name'.")
                                        }
                                        parsedSubtopics.add(Subtopic(id = stId, name = stName))
                                    }
                                }
                            }

                            totalTopics++
                            totalSubtopics += parsedSubtopics.size
                            val trackCount = if (parsedSubtopics.isNotEmpty()) parsedSubtopics.size else 1
                            totalTrackable += trackCount

                            parsedTopics.add(
                                Topic(
                                    id = tId,
                                    name = tName,
                                    description = tDesc,
                                    subtopics = parsedSubtopics
                                )
                            )
                        }

                        totalChapters++
                        parsedChapters.add(
                            Chapter(
                                id = cId,
                                name = cName,
                                description = cDesc,
                                topics = parsedTopics
                            )
                        )
                    }

                    summaryList.add(
                        "$sName (${parsedChapters.size} chapters, ${parsedChapters.sumOf { it.totalTrackableItems }} trackable items)"
                    )

                    parsedSubjects.add(
                        Subject(
                            id = sId,
                            name = sName,
                            description = sDesc,
                            chapters = parsedChapters
                        )
                    )
                }

                return ValidationResult(
                    isValid = errors.isEmpty(),
                    errors = errors,
                    warnings = warnings,
                    type = "academics",
                    rootCount = parsedSubjects.size,
                    containerCount = totalChapters,
                    topicCount = totalTopics,
                    subtopicCount = totalSubtopics,
                    totalTrackableItems = totalTrackable,
                    summaryHierarchy = summaryList,
                    parsedSubjects = parsedSubjects,
                    rawJson = rawJson
                )
            }

            "skills" -> {
                if (!root.has("skills")) {
                    errors.add("Missing required array: 'skills'.")
                    return ValidationResult(isValid = false, errors = errors, rawJson = rawJson)
                }

                val skillsArr = root.optJSONArray("skills")
                if (skillsArr == null) {
                    errors.add("'skills' must be a valid JSON array.")
                    return ValidationResult(isValid = false, errors = errors, rawJson = rawJson)
                }

                val parsedSkills = mutableListOf<Skill>()
                val summaryList = mutableListOf<String>()
                var totalStages = 0
                var totalTopics = 0
                var totalSubtopics = 0
                var totalTrackable = 0

                for (sIdx in 0 until skillsArr.length()) {
                    val sObj = skillsArr.optJSONObject(sIdx)
                    if (sObj == null) {
                        errors.add("Skill at index $sIdx is not a valid JSON object.")
                        continue
                    }

                    val sId = sObj.optString("id", "").trim()
                    if (sId.isEmpty()) {
                        errors.add("Skill at index $sIdx has a missing or empty 'id'.")
                    } else if (!seenIds.add(sId)) {
                        errors.add("Duplicate ID '$sId' detected on Skill at index $sIdx.")
                    }

                    val sName = sObj.optString("name", "").trim()
                    if (sName.isEmpty()) {
                        errors.add("Skill '$sId' (index $sIdx) has a missing or empty 'name'.")
                    }

                    val sDesc = if (sObj.has("description")) sObj.optString("description") else null

                    if (!sObj.has("stages")) {
                        errors.add("Skill '$sName' is missing required 'stages' array.")
                        continue
                    }
                    val stagesArr = sObj.optJSONArray("stages")
                    if (stagesArr == null) {
                        errors.add("Skill '$sName' has an invalid 'stages' field (must be an array).")
                        continue
                    }

                    val parsedStages = mutableListOf<Stage>()

                    for (stIdx in 0 until stagesArr.length()) {
                        val stObj = stagesArr.optJSONObject(stIdx)
                        if (stObj == null) {
                            errors.add("Stage at index $stIdx in Skill '$sName' is not a valid object.")
                            continue
                        }

                        val stId = stObj.optString("id", "").trim()
                        if (stId.isEmpty()) {
                            errors.add("Stage at index $stIdx in Skill '$sName' has a missing or empty 'id'.")
                        } else if (!seenIds.add(stId)) {
                            errors.add("Duplicate ID '$stId' detected on Stage in Skill '$sName'.")
                        }

                        val stName = stObj.optString("name", "").trim()
                        if (stName.isEmpty()) {
                            errors.add("Stage '$stId' in Skill '$sName' has a missing or empty 'name'.")
                        }

                        val stDesc = if (stObj.has("description")) stObj.optString("description") else null

                        if (!stObj.has("topics")) {
                            errors.add("Stage '$stName' is missing required 'topics' array.")
                            continue
                        }
                        val topicsArr = stObj.optJSONArray("topics")
                        if (topicsArr == null) {
                            errors.add("Stage '$stName' has an invalid 'topics' field (must be an array).")
                            continue
                        }

                        val parsedTopics = mutableListOf<Topic>()

                        for (tIdx in 0 until topicsArr.length()) {
                            val tObj = topicsArr.optJSONObject(tIdx)
                            if (tObj == null) {
                                errors.add("Topic at index $tIdx in Stage '$stName' is not a valid object.")
                                continue
                            }

                            val tId = tObj.optString("id", "").trim()
                            if (tId.isEmpty()) {
                                errors.add("Topic at index $tIdx in Stage '$stName' has a missing or empty 'id'.")
                            } else if (!seenIds.add(tId)) {
                                errors.add("Duplicate ID '$tId' detected on Topic in Stage '$stName'.")
                            }

                            val tName = tObj.optString("name", "").trim()
                            if (tName.isEmpty()) {
                                errors.add("Topic '$tId' in Stage '$stName' has a missing or empty 'name'.")
                            }

                            val tDesc = if (tObj.has("description")) tObj.optString("description") else null

                            val parsedSubtopics = mutableListOf<Subtopic>()
                            if (tObj.has("subtopics")) {
                                val subArr = tObj.optJSONArray("subtopics")
                                if (subArr == null) {
                                    errors.add("Topic '$tName' has an invalid 'subtopics' field (must be an array).")
                                } else {
                                    for (subIdx in 0 until subArr.length()) {
                                        val subObj = subArr.optJSONObject(subIdx)
                                        if (subObj == null) {
                                            errors.add("Subtopic at index $subIdx in Topic '$tName' is not a valid object.")
                                            continue
                                        }
                                        val subId = subObj.optString("id", "").trim()
                                        if (subId.isEmpty()) {
                                            errors.add("Subtopic at index $subIdx in Topic '$tName' has missing or empty 'id'.")
                                        } else if (!seenIds.add(subId)) {
                                            errors.add("Duplicate ID '$subId' detected on Subtopic in Topic '$tName'.")
                                        }
                                        val subName = subObj.optString("name", "").trim()
                                        if (subName.isEmpty()) {
                                            errors.add("Subtopic '$subId' in Topic '$tName' has missing or empty 'name'.")
                                        }
                                        parsedSubtopics.add(Subtopic(id = subId, name = subName))
                                    }
                                }
                            }

                            totalTopics++
                            totalSubtopics += parsedSubtopics.size
                            val trackCount = if (parsedSubtopics.isNotEmpty()) parsedSubtopics.size else 1
                            totalTrackable += trackCount

                            parsedTopics.add(
                                Topic(
                                    id = tId,
                                    name = tName,
                                    description = tDesc,
                                    subtopics = parsedSubtopics
                                )
                            )
                        }

                        totalStages++
                        parsedStages.add(
                            Stage(
                                id = stId,
                                name = stName,
                                description = stDesc,
                                topics = parsedTopics
                            )
                        )
                    }

                    summaryList.add(
                        "$sName (${parsedStages.size} stages, ${parsedStages.sumOf { it.totalTrackableItems }} trackable items)"
                    )

                    parsedSkills.add(
                        Skill(
                            id = sId,
                            name = sName,
                            description = sDesc,
                            stages = parsedStages
                        )
                    )
                }

                return ValidationResult(
                    isValid = errors.isEmpty(),
                    errors = errors,
                    warnings = warnings,
                    type = "skills",
                    rootCount = parsedSkills.size,
                    containerCount = totalStages,
                    topicCount = totalTopics,
                    subtopicCount = totalSubtopics,
                    totalTrackableItems = totalTrackable,
                    summaryHierarchy = summaryList,
                    parsedSkills = parsedSkills,
                    rawJson = rawJson
                )
            }

            "still_backup" -> {
                val parsedSubjects = mutableListOf<Subject>()
                val parsedSkills = mutableListOf<Skill>()
                val summaryList = mutableListOf<String>()

                if (root.has("academics")) {
                    val acadObj = root.optJSONObject("academics")
                    if (acadObj != null) {
                        val acadRes = validateAndParse(acadObj.toString())
                        if (acadRes.isValid) {
                            parsedSubjects.addAll(acadRes.parsedSubjects)
                            summaryList.add("Academics: ${acadRes.rootCount} subjects, ${acadRes.totalTrackableItems} items")
                        } else {
                            errors.addAll(acadRes.errors)
                        }
                    }
                }

                if (root.has("skills")) {
                    val skObj = root.optJSONObject("skills")
                    if (skObj != null) {
                        val skRes = validateAndParse(skObj.toString())
                        if (skRes.isValid) {
                            parsedSkills.addAll(skRes.parsedSkills)
                            summaryList.add("Skills: ${skRes.rootCount} skills, ${skRes.totalTrackableItems} items")
                        } else {
                            errors.addAll(skRes.errors)
                        }
                    }
                }

                if (parsedSubjects.isEmpty() && parsedSkills.isEmpty() && errors.isEmpty()) {
                    errors.add("Backup file contains neither valid 'academics' nor 'skills' definitions.")
                }

                return ValidationResult(
                    isValid = errors.isEmpty(),
                    errors = errors,
                    warnings = warnings,
                    type = "still_backup",
                    rootCount = parsedSubjects.size + parsedSkills.size,
                    containerCount = parsedSubjects.sumOf { it.chapters.size } + parsedSkills.sumOf { it.stages.size },
                    topicCount = parsedSubjects.sumOf { s -> s.chapters.sumOf { c -> c.topics.size } } +
                            parsedSkills.sumOf { sk -> sk.stages.sumOf { st -> st.topics.size } },
                    subtopicCount = parsedSubjects.sumOf { s -> s.chapters.sumOf { c -> c.topics.sumOf { t -> t.subtopics.size } } } +
                            parsedSkills.sumOf { sk -> sk.stages.sumOf { st -> st.topics.sumOf { t -> t.subtopics.size } } },
                    totalTrackableItems = parsedSubjects.sumOf { it.totalTrackableItems } + parsedSkills.sumOf { it.totalTrackableItems },
                    summaryHierarchy = summaryList,
                    parsedSubjects = parsedSubjects,
                    parsedSkills = parsedSkills,
                    rawJson = rawJson
                )
            }

            else -> {
                return ValidationResult(
                    isValid = false,
                    errors = listOf("Unsupported root curriculum type: $type"),
                    rawJson = rawJson
                )
            }
        }
    }

    // --- CANONICAL EXPORTERS ---

    fun exportAcademicsJson(subjects: List<Subject>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("type", "academics")

        val sArr = JSONArray()
        subjects.forEach { s ->
            val sObj = JSONObject()
            sObj.put("id", s.id)
            sObj.put("name", s.name)
            s.description?.let { if (it.isNotBlank()) sObj.put("description", it) }

            val cArr = JSONArray()
            s.chapters.forEach { c ->
                val cObj = JSONObject()
                cObj.put("id", c.id)
                cObj.put("name", c.name)
                c.description?.let { if (it.isNotBlank()) cObj.put("description", it) }

                val tArr = JSONArray()
                c.topics.forEach { t ->
                    val tObj = JSONObject()
                    tObj.put("id", t.id)
                    tObj.put("name", t.name)
                    t.description?.let { if (it.isNotBlank()) tObj.put("description", it) }

                    if (t.subtopics.isNotEmpty()) {
                        val stArr = JSONArray()
                        t.subtopics.forEach { st ->
                            val stObj = JSONObject()
                            stObj.put("id", st.id)
                            stObj.put("name", st.name)
                            stArr.put(stObj)
                        }
                        tObj.put("subtopics", stArr)
                    }
                    tArr.put(tObj)
                }
                cObj.put("topics", tArr)
                cArr.put(cObj)
            }
            sObj.put("chapters", cArr)
            sArr.put(sObj)
        }
        root.put("subjects", sArr)
        return root.toString(2)
    }

    fun exportSkillsJson(skills: List<Skill>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("type", "skills")

        val sArr = JSONArray()
        skills.forEach { sk ->
            val sObj = JSONObject()
            sObj.put("id", sk.id)
            sObj.put("name", sk.name)
            sk.description?.let { if (it.isNotBlank()) sObj.put("description", it) }

            val stArr = JSONArray()
            sk.stages.forEach { st ->
                val stObj = JSONObject()
                stObj.put("id", st.id)
                stObj.put("name", st.name)
                st.description?.let { if (it.isNotBlank()) stObj.put("description", it) }

                val tArr = JSONArray()
                st.topics.forEach { t ->
                    val tObj = JSONObject()
                    tObj.put("id", t.id)
                    tObj.put("name", t.name)
                    t.description?.let { if (it.isNotBlank()) tObj.put("description", it) }

                    if (t.subtopics.isNotEmpty()) {
                        val subArr = JSONArray()
                        t.subtopics.forEach { sub ->
                            val subObj = JSONObject()
                            subObj.put("id", sub.id)
                            subObj.put("name", sub.name)
                            subArr.put(subObj)
                        }
                        tObj.put("subtopics", subArr)
                    }
                    tArr.put(tObj)
                }
                stObj.put("topics", tArr)
                stArr.put(stObj)
            }
            sObj.put("stages", stArr)
            sArr.put(sObj)
        }
        root.put("skills", sArr)
        return root.toString(2)
    }

    fun exportProgressJson(type: String, progress: Map<String, ProgressRecord>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("type", "${type}Progress")
        val pObj = JSONObject()
        progress.forEach { (id, rec) ->
            val rObj = JSONObject()
            rObj.put("completed", rec.completed)
            if (rec.completedAt != null) {
                rObj.put("completedAt", rec.completedAt)
            } else {
                rObj.put("completedAt", JSONObject.NULL)
            }
            pObj.put(id, rObj)
        }
        root.put("progress", pObj)
        return root.toString(2)
    }

    fun exportFullBackup(
        subjects: List<Subject>,
        skills: List<Skill>,
        academicsProgress: Map<String, ProgressRecord>,
        skillsProgress: Map<String, ProgressRecord>,
        sessions: List<StudySession>
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("type", "still_backup")
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        root.put("exportedAt", sdf.format(Date()))

        // Academics
        root.put("academics", JSONObject(exportAcademicsJson(subjects)))
        // Skills
        root.put("skills", JSONObject(exportSkillsJson(skills)))

        // Academics Progress
        val apObj = JSONObject()
        academicsProgress.forEach { (id, rec) ->
            apObj.put(id, JSONObject().apply {
                put("completed", rec.completed)
                put("completedAt", rec.completedAt ?: JSONObject.NULL)
            })
        }
        root.put("academicsProgress", apObj)

        // Skills Progress
        val spObj = JSONObject()
        skillsProgress.forEach { (id, rec) ->
            spObj.put(id, JSONObject().apply {
                put("completed", rec.completed)
                put("completedAt", rec.completedAt ?: JSONObject.NULL)
            })
        }
        root.put("skillsProgress", spObj)

        // Sessions
        val sessArr = JSONArray()
        sessions.forEach { s ->
            sessArr.put(JSONObject().apply {
                put("id", s.id)
                put("category", s.category.name)
                put("targetId", s.targetId)
                put("targetName", s.targetName)
                s.chapterOrStageId?.let { put("chapterOrStageId", it) }
                s.chapterOrStageName?.let { put("chapterOrStageName", it) }
                s.topicId?.let { put("topicId", it) }
                s.topicName?.let { put("topicName", it) }
                s.subTargetName?.let { put("subTargetName", it) }
                put("startTime", s.startTime)
                put("endTime", s.endTime)
                s.plannedDurationMinutes?.let { put("plannedDurationMinutes", it) }
                put("pausedDurationSeconds", s.pausedDurationSeconds)
                put("notes", s.notes)
                put("status", s.status.name)
            })
        }
        root.put("sessions", sessArr)

        return root.toString(2)
    }
}

package com.example.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class StillRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("still_student_tracker_prefs_v2", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SUBJECTS = "key_canonical_subjects_v1"
        private const val KEY_SKILLS = "key_canonical_skills_v1"
        private const val KEY_ACADEMICS_PROGRESS = "key_academics_progress_v1"
        private const val KEY_SKILLS_PROGRESS = "key_skills_progress_v1"
        private const val KEY_SESSIONS = "key_sessions_v2"
        private const val KEY_ACTIVE_SESSION = "key_active_session_v2"
        private const val KEY_DISTRACTIONS = "key_distractions_v2"
        private const val KEY_SETTINGS = "key_settings_v2"
        private const val KEY_PROFILE_NAME = "key_profile_name_v1"
    }

    init {
        // First launch initialization: check if initialized
        if (!prefs.contains(KEY_SUBJECTS)) {
            loadCanonicalDemoData()
        }
    }

    // --- PROFILE NAME STORAGE (Independent & Local) ---
    fun getUserName(): String? {
        val direct = prefs.getString(KEY_PROFILE_NAME, null)?.trim()
        if (!direct.isNullOrEmpty()) return direct
        val settingsName = getSettings().studentName.trim()
        return settingsName.takeIf { it.isNotEmpty() }
    }

    fun saveUserName(name: String) {
        val trimmed = name.trim()
        prefs.edit().putString(KEY_PROFILE_NAME, trimmed).apply()
        val currentSettings = getSettings()
        saveSettings(currentSettings.copy(studentName = trimmed))
    }

    fun hasUserName(): Boolean {
        return !getUserName().isNullOrBlank()
    }

    fun clearUserName() {
        prefs.edit().remove(KEY_PROFILE_NAME).apply()
        val currentSettings = getSettings()
        saveSettings(currentSettings.copy(studentName = ""))
    }

    // --- RESET & DEMO ACTIONS ---
    fun loadCanonicalDemoData() {
        val canonicalSubjects = SampleData.getCanonicalSubjects()
        val canonicalSkills = SampleData.getCanonicalSkills()
        saveSubjects(canonicalSubjects)
        saveSkills(canonicalSkills)

        // Seed sample progress for demonstration
        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
        val acadProgress = mutableMapOf(
            "limit-definition" to ProgressRecord(completed = true, completedAt = nowIso),
            "limit-laws" to ProgressRecord(completed = false, completedAt = null),
            "continuity" to ProgressRecord(completed = false, completedAt = null)
        )
        val skillProgress = mutableMapOf(
            "html-elements" to ProgressRecord(completed = true, completedAt = nowIso),
            "html-attributes" to ProgressRecord(completed = true, completedAt = nowIso),
            "css-basics" to ProgressRecord(completed = false, completedAt = null)
        )
        saveAcademicsProgress(acadProgress)
        saveSkillsProgress(skillProgress)
        saveDistractionApps(SampleData.getSampleDistractionApps())
        saveSessions(SampleData.getSampleSessions())
    }

    fun clearAllData() {
        saveSubjects(emptyList())
        saveSkills(emptyList())
        saveAcademicsProgress(emptyMap())
        saveSkillsProgress(emptyMap())
        saveDistractionApps(emptyList())
        saveSessions(emptyList())
        clearActiveSession()
        clearUserName()
    }

    // --- ACADEMICS CURRICULUM STORAGE ---
    fun getSubjects(): List<Subject> {
        val jsonStr = prefs.getString(KEY_SUBJECTS, null) ?: return emptyList()
        return try {
            val result = CurriculumJsonParser.validateAndParse(jsonStr)
            if (result.isValid) result.parsedSubjects else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveSubjects(subjects: List<Subject>) {
        val jsonStr = CurriculumJsonParser.exportAcademicsJson(subjects)
        prefs.edit().putString(KEY_SUBJECTS, jsonStr).apply()
    }

    // --- SKILLS CURRICULUM STORAGE ---
    fun getSkills(): List<Skill> {
        val jsonStr = prefs.getString(KEY_SKILLS, null) ?: return emptyList()
        return try {
            val result = CurriculumJsonParser.validateAndParse(jsonStr)
            if (result.isValid) result.parsedSkills else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveSkills(skills: List<Skill>) {
        val jsonStr = CurriculumJsonParser.exportSkillsJson(skills)
        prefs.edit().putString(KEY_SKILLS, jsonStr).apply()
    }

    // --- SEPARATE PROGRESS STORES ---
    fun getAcademicsProgress(): Map<String, ProgressRecord> {
        val jsonStr = prefs.getString(KEY_ACADEMICS_PROGRESS, null) ?: return emptyMap()
        return parseProgressMap(jsonStr)
    }

    fun saveAcademicsProgress(map: Map<String, ProgressRecord>) {
        val jsonStr = serializeProgressMap(map)
        prefs.edit().putString(KEY_ACADEMICS_PROGRESS, jsonStr).apply()
    }

    fun getSkillsProgress(): Map<String, ProgressRecord> {
        val jsonStr = prefs.getString(KEY_SKILLS_PROGRESS, null) ?: return emptyMap()
        return parseProgressMap(jsonStr)
    }

    fun saveSkillsProgress(map: Map<String, ProgressRecord>) {
        val jsonStr = serializeProgressMap(map)
        prefs.edit().putString(KEY_SKILLS_PROGRESS, jsonStr).apply()
    }

    private fun parseProgressMap(jsonStr: String): Map<String, ProgressRecord> {
        return try {
            val root = JSONObject(jsonStr)
            val pObj = if (root.has("progress")) root.getJSONObject("progress") else root
            val map = mutableMapOf<String, ProgressRecord>()
            val keys = pObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val item = pObj.getJSONObject(key)
                val comp = item.optBoolean("completed", false)
                val compAt = if (item.has("completedAt") && !item.isNull("completedAt")) item.getString("completedAt") else null
                map[key] = ProgressRecord(completed = comp, completedAt = compAt)
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun serializeProgressMap(map: Map<String, ProgressRecord>): String {
        val root = JSONObject()
        map.forEach { (id, rec) ->
            root.put(id, JSONObject().apply {
                put("completed", rec.completed)
                put("completedAt", rec.completedAt ?: JSONObject.NULL)
            })
        }
        return root.toString()
    }

    // --- TOGGLE COMPLETION ---
    fun toggleAcademicItem(itemId: String): Map<String, ProgressRecord> {
        val current = getAcademicsProgress().toMutableMap()
        val prev = current[itemId]?.completed ?: false
        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
        if (!prev) {
            current[itemId] = ProgressRecord(completed = true, completedAt = nowIso)
        } else {
            current[itemId] = ProgressRecord(completed = false, completedAt = null)
        }
        saveAcademicsProgress(current)
        return current
    }

    fun toggleSkillItem(itemId: String): Map<String, ProgressRecord> {
        val current = getSkillsProgress().toMutableMap()
        val prev = current[itemId]?.completed ?: false
        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
        if (!prev) {
            current[itemId] = ProgressRecord(completed = true, completedAt = nowIso)
        } else {
            current[itemId] = ProgressRecord(completed = false, completedAt = null)
        }
        saveSkillsProgress(current)
        return current
    }

    // --- PROGRESS CALCULATION HELPERS ---

    fun isTopicComplete(topic: Topic, progress: Map<String, ProgressRecord>): Boolean {
        return if (topic.hasSubtopics) {
            topic.subtopics.isNotEmpty() && topic.subtopics.all { sub ->
                progress[sub.id]?.completed == true
            }
        } else {
            progress[topic.id]?.completed == true
        }
    }

    fun getTopicProgressSummary(topic: Topic, progress: Map<String, ProgressRecord>): ProgressSummary {
        return if (topic.hasSubtopics) {
            val total = topic.subtopics.size
            val done = topic.subtopics.count { progress[it.id]?.completed == true }
            ProgressSummary(done, total)
        } else {
            val done = if (progress[topic.id]?.completed == true) 1 else 0
            ProgressSummary(done, 1)
        }
    }

    fun getChapterProgressSummary(chapter: Chapter, progress: Map<String, ProgressRecord>): ProgressSummary {
        val total = chapter.totalTrackableItems
        var completed = 0
        chapter.topics.forEach { t ->
            completed += getTopicProgressSummary(t, progress).completedCount
        }
        return ProgressSummary(completed, total)
    }

    fun getSubjectProgressSummary(subject: Subject, progress: Map<String, ProgressRecord>): ProgressSummary {
        val total = subject.totalTrackableItems
        var completed = 0
        subject.chapters.forEach { c ->
            completed += getChapterProgressSummary(c, progress).completedCount
        }
        return ProgressSummary(completed, total)
    }

    fun getOverallAcademicsProgress(): ProgressSummary {
        val subjects = getSubjects()
        val progress = getAcademicsProgress()
        val total = subjects.sumOf { it.totalTrackableItems }
        var completed = 0
        subjects.forEach { s ->
            completed += getSubjectProgressSummary(s, progress).completedCount
        }
        return ProgressSummary(completed, total)
    }

    fun getStageProgressSummary(stage: Stage, progress: Map<String, ProgressRecord>): ProgressSummary {
        val total = stage.totalTrackableItems
        var completed = 0
        stage.topics.forEach { t ->
            completed += getTopicProgressSummary(t, progress).completedCount
        }
        return ProgressSummary(completed, total)
    }

    fun getSkillProgressSummary(skill: Skill, progress: Map<String, ProgressRecord>): ProgressSummary {
        val total = skill.totalTrackableItems
        var completed = 0
        skill.stages.forEach { st ->
            completed += getStageProgressSummary(st, progress).completedCount
        }
        return ProgressSummary(completed, total)
    }

    fun getOverallSkillsProgress(): ProgressSummary {
        val skills = getSkills()
        val progress = getSkillsProgress()
        val total = skills.sumOf { it.totalTrackableItems }
        var completed = 0
        skills.forEach { sk ->
            completed += getSkillProgressSummary(sk, progress).completedCount
        }
        return ProgressSummary(completed, total)
    }

    // --- CURRICULUM IMPORT & APPLY ---

    fun applyImport(result: ValidationResult, replaceExisting: Boolean) {
        if (!result.isValid) return

        when (result.type) {
            "academics" -> {
                val existingSubjects = getSubjects()
                val existingProgress = getAcademicsProgress()

                val finalSubjects = if (replaceExisting) {
                    result.parsedSubjects
                } else {
                    // Merge by matching ID: preserve matching progress, append or update items
                    val map = existingSubjects.associateBy { it.id }.toMutableMap()
                    result.parsedSubjects.forEach { importedSubject ->
                        map[importedSubject.id] = importedSubject
                    }
                    map.values.toList()
                }

                saveSubjects(finalSubjects)
                // Filter or preserve compatible progress
                val validTrackableIds = mutableSetOf<String>()
                finalSubjects.forEach { s ->
                    s.chapters.forEach { c ->
                        c.topics.forEach { t ->
                            if (t.hasSubtopics) {
                                t.subtopics.forEach { validTrackableIds.add(it.id) }
                            } else {
                                validTrackableIds.add(t.id)
                            }
                        }
                    }
                }
                val preservedProgress = existingProgress.filterKeys { it in validTrackableIds }
                saveAcademicsProgress(preservedProgress)
            }

            "skills" -> {
                val existingSkills = getSkills()
                val existingProgress = getSkillsProgress()

                val finalSkills = if (replaceExisting) {
                    result.parsedSkills
                } else {
                    val map = existingSkills.associateBy { it.id }.toMutableMap()
                    result.parsedSkills.forEach { importedSkill ->
                        map[importedSkill.id] = importedSkill
                    }
                    map.values.toList()
                }

                saveSkills(finalSkills)
                val validTrackableIds = mutableSetOf<String>()
                finalSkills.forEach { sk ->
                    sk.stages.forEach { st ->
                        st.topics.forEach { t ->
                            if (t.hasSubtopics) {
                                t.subtopics.forEach { validTrackableIds.add(it.id) }
                            } else {
                                validTrackableIds.add(t.id)
                            }
                        }
                    }
                }
                val preservedProgress = existingProgress.filterKeys { it in validTrackableIds }
                saveSkillsProgress(preservedProgress)
            }

            "still_backup" -> {
                if (result.parsedSubjects.isNotEmpty()) {
                    val existingSubjects = getSubjects()
                    val finalSubjects = if (replaceExisting) {
                        result.parsedSubjects
                    } else {
                        val map = existingSubjects.associateBy { it.id }.toMutableMap()
                        result.parsedSubjects.forEach { map[it.id] = it }
                        map.values.toList()
                    }
                    saveSubjects(finalSubjects)
                }

                if (result.parsedSkills.isNotEmpty()) {
                    val existingSkills = getSkills()
                    val finalSkills = if (replaceExisting) {
                        result.parsedSkills
                    } else {
                        val map = existingSkills.associateBy { it.id }.toMutableMap()
                        result.parsedSkills.forEach { map[it.id] = it }
                        map.values.toList()
                    }
                    saveSkills(finalSkills)
                }

                // Restore progress from backup if present
                try {
                    val root = org.json.JSONObject(result.rawJson)
                    if (root.has("academicsProgress")) {
                        val apObj = root.getJSONObject("academicsProgress")
                        val currentAp = if (replaceExisting) mutableMapOf() else getAcademicsProgress().toMutableMap()
                        val keys = apObj.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val item = apObj.getJSONObject(k)
                            currentAp[k] = ProgressRecord(
                                completed = item.optBoolean("completed", false),
                                completedAt = if (item.has("completedAt") && !item.isNull("completedAt")) item.getString("completedAt") else null
                            )
                        }
                        saveAcademicsProgress(currentAp)
                    }
                    if (root.has("skillsProgress")) {
                        val spObj = root.getJSONObject("skillsProgress")
                        val currentSp = if (replaceExisting) mutableMapOf() else getSkillsProgress().toMutableMap()
                        val keys = spObj.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val item = spObj.getJSONObject(k)
                            currentSp[k] = ProgressRecord(
                                completed = item.optBoolean("completed", false),
                                completedAt = if (item.has("completedAt") && !item.isNull("completedAt")) item.getString("completedAt") else null
                            )
                        }
                        saveSkillsProgress(currentSp)
                    }
                } catch (e: Exception) {
                    // Safe fallback
                }
            }
        }
    }

    // --- SESSIONS ---
    fun getSessions(): List<StudySession> {
        val jsonStr = prefs.getString(KEY_SESSIONS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<StudySession>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    StudySession(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        category = SessionCategory.valueOf(o.optString("category", SessionCategory.ACADEMIC.name)),
                        targetId = o.optString("targetId", ""),
                        targetName = o.optString("targetName", "Session"),
                        chapterOrStageId = if (o.has("chapterOrStageId")) o.getString("chapterOrStageId") else null,
                        chapterOrStageName = if (o.has("chapterOrStageName")) o.getString("chapterOrStageName") else null,
                        topicId = if (o.has("topicId")) o.getString("topicId") else null,
                        topicName = if (o.has("topicName")) o.getString("topicName") else null,
                        subTargetName = if (o.has("subTargetName")) o.getString("subTargetName") else null,
                        startTime = o.optLong("startTime", System.currentTimeMillis()),
                        endTime = o.optLong("endTime", System.currentTimeMillis()),
                        plannedDurationMinutes = if (o.has("plannedDurationMinutes")) o.getInt("plannedDurationMinutes") else null,
                        pausedDurationSeconds = o.optLong("pausedDurationSeconds", 0L),
                        notes = o.optString("notes", ""),
                        status = SessionStatus.valueOf(o.optString("status", SessionStatus.COMPLETED.name))
                    )
                )
            }
            list.sortedByDescending { it.startTime }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveSessions(sessions: List<StudySession>) {
        val arr = JSONArray()
        sessions.forEach { s ->
            arr.put(JSONObject().apply {
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
        prefs.edit().putString(KEY_SESSIONS, arr.toString()).apply()
    }

    fun recordCompletedSession(session: StudySession) {
        val list = getSessions().toMutableList()
        list.add(0, session)
        saveSessions(list)
    }

    fun deleteSession(sessionId: String): List<StudySession> {
        val list = getSessions().filterNot { it.id == sessionId }
        saveSessions(list)
        return list
    }

    // --- ACTIVE SESSION ENGINE ---
    fun getActiveSession(): ActiveSession? {
        val jsonStr = prefs.getString(KEY_ACTIVE_SESSION, null) ?: return null
        return try {
            val o = JSONObject(jsonStr)
            ActiveSession(
                id = o.getString("id"),
                category = SessionCategory.valueOf(o.getString("category")),
                targetId = o.getString("targetId"),
                targetName = o.getString("targetName"),
                chapterOrStageId = if (o.has("chapterOrStageId")) o.getString("chapterOrStageId") else null,
                chapterOrStageName = if (o.has("chapterOrStageName")) o.getString("chapterOrStageName") else null,
                topicId = if (o.has("topicId")) o.getString("topicId") else null,
                topicName = if (o.has("topicName")) o.getString("topicName") else null,
                subTargetName = if (o.has("subTargetName")) o.getString("subTargetName") else null,
                plannedDurationMinutes = if (o.has("plannedDurationMinutes")) o.getInt("plannedDurationMinutes") else null,
                startTime = o.getLong("startTime"),
                isPaused = o.getBoolean("isPaused"),
                pauseStartTime = if (o.has("pauseStartTime")) o.getLong("pauseStartTime") else null,
                totalPausedSeconds = o.getLong("totalPausedSeconds")
            )
        } catch (e: Exception) {
            null
        }
    }

    fun saveActiveSession(session: ActiveSession) {
        val o = JSONObject().apply {
            put("id", session.id)
            put("category", session.category.name)
            put("targetId", session.targetId)
            put("targetName", session.targetName)
            session.chapterOrStageId?.let { put("chapterOrStageId", it) }
            session.chapterOrStageName?.let { put("chapterOrStageName", it) }
            session.topicId?.let { put("topicId", it) }
            session.topicName?.let { put("topicName", it) }
            session.subTargetName?.let { put("subTargetName", it) }
            session.plannedDurationMinutes?.let { put("plannedDurationMinutes", it) }
            put("startTime", session.startTime)
            put("isPaused", session.isPaused)
            session.pauseStartTime?.let { put("pauseStartTime", it) }
            put("totalPausedSeconds", session.totalPausedSeconds)
        }
        prefs.edit().putString(KEY_ACTIVE_SESSION, o.toString()).apply()
    }

    fun clearActiveSession() {
        prefs.edit().remove(KEY_ACTIVE_SESSION).apply()
    }

    // --- DISTRACTION APPS ---
    fun getDistractionApps(): List<DistractionApp> {
        val jsonStr = prefs.getString(KEY_DISTRACTIONS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<DistractionApp>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    DistractionApp(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        appName = o.optString("appName", "App"),
                        packageName = o.optString("packageName", "com.example"),
                        category = o.optString("category", "Social"),
                        todayUsageMinutes = o.optInt("todayUsageMinutes", 0),
                        limitMinutes = o.optInt("limitMinutes", 30),
                        isBlocked = o.optBoolean("isBlocked", false),
                        iconName = o.optString("iconName", "phone")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveDistractionApps(apps: List<DistractionApp>) {
        val arr = JSONArray()
        apps.forEach { a ->
            arr.put(JSONObject().apply {
                put("id", a.id)
                put("appName", a.appName)
                put("packageName", a.packageName)
                put("category", a.category)
                put("todayUsageMinutes", a.todayUsageMinutes)
                put("limitMinutes", a.limitMinutes)
                put("isBlocked", a.isBlocked)
                put("iconName", a.iconName)
            })
        }
        prefs.edit().putString(KEY_DISTRACTIONS, arr.toString()).apply()
    }

    fun toggleAppBlock(appId: String): List<DistractionApp> {
        val list = getDistractionApps().map { a ->
            if (a.id == appId) a.copy(isBlocked = !a.isBlocked) else a
        }
        saveDistractionApps(list)
        return list
    }

    fun updateAppLimit(appId: String, newLimitMinutes: Int): List<DistractionApp> {
        val list = getDistractionApps().map { a ->
            if (a.id == appId) a.copy(limitMinutes = newLimitMinutes) else a
        }
        saveDistractionApps(list)
        return list
    }

    fun addDistractionApp(name: String, category: String, limitMinutes: Int): List<DistractionApp> {
        val current = getDistractionApps().toMutableList()
        current.add(
            DistractionApp(
                id = "app_${UUID.randomUUID().toString().take(8)}",
                appName = name,
                packageName = "custom.${name.lowercase().replace(" ", "")}",
                category = category,
                todayUsageMinutes = 0,
                limitMinutes = limitMinutes,
                isBlocked = true,
                iconName = "block"
            )
        )
        saveDistractionApps(current)
        return current
    }

    // --- SETTINGS ---
    fun getSettings(): UserSettings {
        val str = prefs.getString(KEY_SETTINGS, null) ?: return UserSettings()
        return try {
            val o = JSONObject(str)
            UserSettings(
                dailyFocusGoalMinutes = o.optInt("dailyFocusGoalMinutes", 180),
                breakReminderIntervalMinutes = o.optInt("breakReminderIntervalMinutes", 45),
                strictDistractionBlock = o.optBoolean("strictDistractionBlock", true),
                soundEnabled = o.optBoolean("soundEnabled", true),
                hapticsEnabled = o.optBoolean("hapticsEnabled", true),
                studentName = o.optString("studentName", "")
            )
        } catch (e: Exception) {
            UserSettings()
        }
    }

    fun saveSettings(settings: UserSettings) {
        val o = JSONObject().apply {
            put("dailyFocusGoalMinutes", settings.dailyFocusGoalMinutes)
            put("breakReminderIntervalMinutes", settings.breakReminderIntervalMinutes)
            put("strictDistractionBlock", settings.strictDistractionBlock)
            put("soundEnabled", settings.soundEnabled)
            put("hapticsEnabled", settings.hapticsEnabled)
            put("studentName", settings.studentName)
        }
        prefs.edit().putString(KEY_SETTINGS, o.toString()).apply()
    }
}

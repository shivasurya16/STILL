package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class AppScreen {
    SPLASH,
    ONBOARDING,
    NAME_SETUP,
    HOME,
    ACADEMICS,
    SUBJECT_DETAIL,
    TOPIC_CHECKLIST,
    SKILLS,
    SKILL_PROGRESSION, // Used for Skill Detail
    STUDY_SESSION,
    SESSION_HISTORY,
    DISTRACTION_MONITORING,
    PRODUCTIVITY_INTELLIGENCE,
    FOCUS_TRACKING,
    DAILY_OVERVIEW,
    SETTINGS,
    MORE
}

data class ProductivityInsight(
    val title: String,
    val description: String,
    val metricValue: String,
    val rationale: String,
    val type: InsightType
)

enum class InsightType {
    STRENGTH,
    OPPORTUNITY,
    ALERT
}

data class DayFocusSummary(
    val dayLabel: String,
    val dateEpoch: Long,
    val academicMinutes: Int,
    val skillMinutes: Int,
    val totalMinutes: Int
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = StillRepository(application.applicationContext)

    // Current user profile name
    private val _userName = MutableStateFlow(repository.getUserName() ?: "")
    val userName: StateFlow<String> = _userName.asStateFlow()

    // Navigation state: check if profile name exists on startup
    private val _currentScreen = MutableStateFlow(
        if (repository.hasUserName()) AppScreen.HOME else AppScreen.NAME_SETUP
    )
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val navStack = mutableListOf<AppScreen>()

    // Detail selection state
    private val _selectedSubjectId = MutableStateFlow<String?>(null)
    val selectedSubjectId: StateFlow<String?> = _selectedSubjectId.asStateFlow()

    private val _selectedChapterId = MutableStateFlow<String?>(null)
    val selectedChapterId: StateFlow<String?> = _selectedChapterId.asStateFlow()

    private val _selectedSkillId = MutableStateFlow<String?>(null)
    val selectedSkillId: StateFlow<String?> = _selectedSkillId.asStateFlow()

    private val _selectedStageId = MutableStateFlow<String?>(null)
    val selectedStageId: StateFlow<String?> = _selectedStageId.asStateFlow()

    private val _selectedOverviewDate = MutableStateFlow<Long>(System.currentTimeMillis())
    val selectedOverviewDate: StateFlow<Long> = _selectedOverviewDate.asStateFlow()

    // Core data flows
    private val _subjects = MutableStateFlow(repository.getSubjects())
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    private val _skills = MutableStateFlow(repository.getSkills())
    val skills: StateFlow<List<Skill>> = _skills.asStateFlow()

    private val _academicsProgress = MutableStateFlow(repository.getAcademicsProgress())
    val academicsProgress: StateFlow<Map<String, ProgressRecord>> = _academicsProgress.asStateFlow()

    private val _skillsProgress = MutableStateFlow(repository.getSkillsProgress())
    val skillsProgress: StateFlow<Map<String, ProgressRecord>> = _skillsProgress.asStateFlow()

    private val _sessions = MutableStateFlow(repository.getSessions())
    val sessions: StateFlow<List<StudySession>> = _sessions.asStateFlow()

    private val _activeSession = MutableStateFlow(repository.getActiveSession())
    val activeSession: StateFlow<ActiveSession?> = _activeSession.asStateFlow()

    private val _activeSessionElapsedSeconds = MutableStateFlow(0L)
    val activeSessionElapsedSeconds: StateFlow<Long> = _activeSessionElapsedSeconds.asStateFlow()

    private val _distractionApps = MutableStateFlow(repository.getDistractionApps())
    val distractionApps: StateFlow<List<DistractionApp>> = _distractionApps.asStateFlow()

    private val _settings = MutableStateFlow(repository.getSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private val _isFocusShieldEnabled = MutableStateFlow(true)
    val isFocusShieldEnabled: StateFlow<Boolean> = _isFocusShieldEnabled.asStateFlow()

    // Shared JSON validation & preview state
    private val _validationResult = MutableStateFlow<ValidationResult?>(null)
    val validationResult: StateFlow<ValidationResult?> = _validationResult.asStateFlow()

    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    private var timerJob: Job? = null

    init {
        startTimerTicker()
    }

    // --- NAVIGATION ---
    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            navStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (navStack.isNotEmpty()) {
            _currentScreen.value = navStack.removeAt(navStack.size - 1)
            return true
        } else if (_currentScreen.value != AppScreen.HOME && repository.hasUserName()) {
            _currentScreen.value = AppScreen.HOME
            return true
        }
        return false
    }

    fun openSubjectDetail(subjectId: String) {
        _selectedSubjectId.value = subjectId
        navigateTo(AppScreen.SUBJECT_DETAIL)
    }

    fun openTopicChecklist(subjectId: String, chapterId: String) {
        _selectedSubjectId.value = subjectId
        _selectedChapterId.value = chapterId
        navigateTo(AppScreen.TOPIC_CHECKLIST)
    }

    fun openSkillDetail(skillId: String) {
        _selectedSkillId.value = skillId
        navigateTo(AppScreen.SKILL_PROGRESSION)
    }

    // --- PROFILE & STARTUP MANAGEMENT ---
    fun hasSavedName(): Boolean = repository.hasUserName()

    fun saveProfileName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) {
            repository.saveUserName(trimmed)
            _userName.value = trimmed
            _settings.value = repository.getSettings()
            navStack.clear()
            _currentScreen.value = AppScreen.HOME
            showFeedback("Welcome, $trimmed!")
        }
    }

    fun updateProfileName(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isNotEmpty()) {
            repository.saveUserName(trimmed)
            _userName.value = trimmed
            _settings.value = repository.getSettings()
            showFeedback("Profile name updated to $trimmed")
        }
    }

    fun completeOnboarding() {
        navStack.clear()
        if (hasSavedName()) {
            _currentScreen.value = AppScreen.HOME
        } else {
            _currentScreen.value = AppScreen.NAME_SETUP
        }
    }

    // --- TIMER ENGINE ---
    private fun startTimerTicker() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                val current = _activeSession.value
                if (current != null) {
                    _activeSessionElapsedSeconds.value = current.getElapsedSeconds()
                } else {
                    _activeSessionElapsedSeconds.value = 0L
                }
                delay(1000)
            }
        }
    }

    fun startSession(
        category: SessionCategory,
        targetId: String,
        targetName: String,
        chapterOrStageId: String? = null,
        chapterOrStageName: String? = null,
        topicId: String? = null,
        topicName: String? = null,
        plannedMinutes: Int? = 45
    ) {
        val subTargetDisplayName = buildString {
            if (!chapterOrStageName.isNullOrBlank()) append(chapterOrStageName)
            if (!topicName.isNullOrBlank()) {
                if (isNotEmpty()) append(" · ")
                append(topicName)
            }
        }.ifBlank { null }

        val newSession = ActiveSession(
            id = "sess_${UUID.randomUUID().toString().take(8)}",
            category = category,
            targetId = targetId,
            targetName = targetName,
            chapterOrStageId = chapterOrStageId,
            chapterOrStageName = chapterOrStageName,
            topicId = topicId,
            topicName = topicName,
            subTargetName = subTargetDisplayName,
            plannedDurationMinutes = plannedMinutes,
            startTime = System.currentTimeMillis(),
            isPaused = false,
            pauseStartTime = null,
            totalPausedSeconds = 0L
        )
        repository.saveActiveSession(newSession)
        _activeSession.value = newSession
        _activeSessionElapsedSeconds.value = 0L
    }

    fun pauseSession() {
        val current = _activeSession.value ?: return
        if (!current.isPaused) {
            val updated = current.copy(
                isPaused = true,
                pauseStartTime = System.currentTimeMillis()
            )
            repository.saveActiveSession(updated)
            _activeSession.value = updated
        }
    }

    fun resumeSession() {
        val current = _activeSession.value ?: return
        if (current.isPaused && current.pauseStartTime != null) {
            val pauseDuration = (System.currentTimeMillis() - current.pauseStartTime) / 1000
            val updated = current.copy(
                isPaused = false,
                pauseStartTime = null,
                totalPausedSeconds = current.totalPausedSeconds + pauseDuration
            )
            repository.saveActiveSession(updated)
            _activeSession.value = updated
        }
    }

    fun finishSession(notes: String = "") {
        val current = _activeSession.value ?: return
        val now = System.currentTimeMillis()
        var totalPaused = current.totalPausedSeconds
        if (current.isPaused && current.pauseStartTime != null) {
            totalPaused += (now - current.pauseStartTime) / 1000
        }

        val completed = StudySession(
            id = current.id,
            category = current.category,
            targetId = current.targetId,
            targetName = current.targetName,
            chapterOrStageId = current.chapterOrStageId,
            chapterOrStageName = current.chapterOrStageName,
            topicId = current.topicId,
            topicName = current.topicName,
            subTargetName = current.subTargetName,
            startTime = current.startTime,
            endTime = now,
            plannedDurationMinutes = current.plannedDurationMinutes,
            pausedDurationSeconds = totalPaused,
            notes = notes,
            status = SessionStatus.COMPLETED
        )

        repository.recordCompletedSession(completed)
        repository.clearActiveSession()
        _activeSession.value = null
        _activeSessionElapsedSeconds.value = 0L
        _sessions.value = repository.getSessions()
        showFeedback("Session saved! Focus time logged.")
    }

    fun cancelSession() {
        repository.clearActiveSession()
        _activeSession.value = null
        _activeSessionElapsedSeconds.value = 0L
        showFeedback("Session discarded.")
    }

    // --- PROGRESS TOGGLES & QUERIES ---

    fun toggleAcademicItem(itemId: String) {
        val updated = repository.toggleAcademicItem(itemId)
        _academicsProgress.value = updated
    }

    fun toggleSkillItem(itemId: String) {
        val updated = repository.toggleSkillItem(itemId)
        _skillsProgress.value = updated
    }

    fun getSubjectProgress(subject: Subject): ProgressSummary {
        return repository.getSubjectProgressSummary(subject, _academicsProgress.value)
    }

    fun getChapterProgress(chapter: Chapter): ProgressSummary {
        return repository.getChapterProgressSummary(chapter, _academicsProgress.value)
    }

    fun getTopicProgress(topic: Topic): ProgressSummary {
        return repository.getTopicProgressSummary(topic, _academicsProgress.value)
    }

    fun isTopicComplete(topic: Topic): Boolean {
        return repository.isTopicComplete(topic, _academicsProgress.value)
    }

    fun getSkillProgress(skill: Skill): ProgressSummary {
        return repository.getSkillProgressSummary(skill, _skillsProgress.value)
    }

    fun getStageProgress(stage: Stage): ProgressSummary {
        return repository.getStageProgressSummary(stage, _skillsProgress.value)
    }

    fun getOverallAcademicsProgress(): ProgressSummary {
        val subs = _subjects.value
        val progress = _academicsProgress.value
        val total = subs.sumOf { it.totalTrackableItems }
        var completed = 0
        subs.forEach { s ->
            completed += repository.getSubjectProgressSummary(s, progress).completedCount
        }
        return ProgressSummary(completed, total)
    }

    fun getOverallSkillsProgress(): ProgressSummary {
        val sks = _skills.value
        val progress = _skillsProgress.value
        val total = sks.sumOf { it.totalTrackableItems }
        var completed = 0
        sks.forEach { sk ->
            completed += repository.getSkillProgressSummary(sk, progress).completedCount
        }
        return ProgressSummary(completed, total)
    }

    // --- SHARED JSON IMPORT SYSTEM ---

    fun validateJson(rawJson: String): ValidationResult {
        val result = CurriculumJsonParser.validateAndParse(rawJson)
        _validationResult.value = result
        return result
    }

    fun applyImport(replaceExisting: Boolean) {
        val result = _validationResult.value ?: return
        if (!result.isValid) return

        repository.applyImport(result, replaceExisting)
        _subjects.value = repository.getSubjects()
        _skills.value = repository.getSkills()
        _academicsProgress.value = repository.getAcademicsProgress()
        _skillsProgress.value = repository.getSkillsProgress()
        _validationResult.value = null

        val actionLabel = if (replaceExisting) "Replaced existing" else "Imported / Merged"
        val moduleLabel = if (result.type == "academics") "Academics" else "Skills"
        showFeedback("$actionLabel $moduleLabel curriculum successfully!")
    }

    fun dismissValidation() {
        _validationResult.value = null
    }

    // Exporters
    fun getExportAcademicsJson(): String {
        return CurriculumJsonParser.exportAcademicsJson(_subjects.value)
    }

    fun getExportSkillsJson(): String {
        return CurriculumJsonParser.exportSkillsJson(_skills.value)
    }

    fun getExportAcademicsProgressJson(): String {
        return CurriculumJsonParser.exportProgressJson("academics", _academicsProgress.value)
    }

    fun getExportSkillsProgressJson(): String {
        return CurriculumJsonParser.exportProgressJson("skills", _skillsProgress.value)
    }

    fun getExportFullBackupJson(): String {
        return CurriculumJsonParser.exportFullBackup(
            subjects = _subjects.value,
            skills = _skills.value,
            academicsProgress = _academicsProgress.value,
            skillsProgress = _skillsProgress.value,
            sessions = _sessions.value
        )
    }

    fun loadDemoCanonicalData() {
        repository.loadCanonicalDemoData()
        _subjects.value = repository.getSubjects()
        _skills.value = repository.getSkills()
        _academicsProgress.value = repository.getAcademicsProgress()
        _skillsProgress.value = repository.getSkillsProgress()
        _sessions.value = repository.getSessions()
        _distractionApps.value = repository.getDistractionApps()
        showFeedback("Loaded canonical demonstration curriculum & progress.")
    }

    fun clearAllData() {
        repository.clearAllData()
        _userName.value = ""
        _subjects.value = emptyList()
        _skills.value = emptyList()
        _academicsProgress.value = emptyMap()
        _skillsProgress.value = emptyMap()
        _sessions.value = emptyList()
        _activeSession.value = null
        _activeSessionElapsedSeconds.value = 0L
        _distractionApps.value = emptyList()
        _settings.value = repository.getSettings()
        navStack.clear()
        _currentScreen.value = AppScreen.NAME_SETUP
        showFeedback("All local data cleared. Welcome to STILL setup.")
    }

    // Distraction actions
    fun toggleAppBlock(appId: String) {
        val updated = repository.toggleAppBlock(appId)
        _distractionApps.value = updated
    }

    fun updateAppLimit(appId: String, newLimitMinutes: Int) {
        val updated = repository.updateAppLimit(appId, newLimitMinutes)
        _distractionApps.value = updated
    }

    fun addDistractionApp(name: String, category: String, limitMinutes: Int) {
        val updated = repository.addDistractionApp(name, category, limitMinutes)
        _distractionApps.value = updated
        showFeedback("App added to distraction shield.")
    }

    fun toggleFocusShield() {
        _isFocusShieldEnabled.value = !_isFocusShieldEnabled.value
    }

    fun deleteSession(sessionId: String) {
        val updated = repository.deleteSession(sessionId)
        _sessions.value = updated
        showFeedback("Session record deleted.")
    }

    fun updateSettings(newSettings: UserSettings) {
        repository.saveSettings(newSettings)
        _settings.value = newSettings
        showFeedback("Preferences updated.")
    }

    fun setOverviewDate(epoch: Long) {
        _selectedOverviewDate.value = epoch
    }

    private fun showFeedback(msg: String) {
        _userFeedbackMessage.value = msg
    }

    fun clearFeedback() {
        _userFeedbackMessage.value = null
    }

    // --- ANALYTICS CALCULATIONS ---

    fun getTodayFocusedMinutes(): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val startOfDay = cal.timeInMillis

        return _sessions.value
            .filter { it.startTime >= startOfDay && it.status == SessionStatus.COMPLETED }
            .sumOf { it.actualDurationMinutes }
    }

    fun getWeeklyFocusSummaries(): List<DayFocusSummary> {
        val result = mutableListOf<DayFocusSummary>()
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())

        for (i in 6 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            c.set(Calendar.HOUR_OF_DAY, 0)
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            val dayStart = c.timeInMillis

            c.set(Calendar.HOUR_OF_DAY, 23)
            c.set(Calendar.MINUTE, 59)
            c.set(Calendar.SECOND, 59)
            val dayEnd = c.timeInMillis

            val daySessions = _sessions.value.filter { it.startTime in dayStart..dayEnd }
            val acadMins = daySessions.filter { it.category == SessionCategory.ACADEMIC }.sumOf { it.actualDurationMinutes }
            val skillMins = daySessions.filter { it.category == SessionCategory.SKILL }.sumOf { it.actualDurationMinutes }

            result.add(
                DayFocusSummary(
                    dayLabel = sdf.format(Date(dayStart)),
                    dateEpoch = dayStart,
                    academicMinutes = acadMins,
                    skillMinutes = skillMins,
                    totalMinutes = acadMins + skillMins
                )
            )
        }
        return result
    }

    fun getProductivityInsights(): List<ProductivityInsight> {
        val sessionsList = _sessions.value
        val insights = mutableListOf<ProductivityInsight>()

        if (sessionsList.isEmpty()) {
            insights.add(
                ProductivityInsight(
                    title = "Start Your First Session",
                    description = "Productivity intelligence will analyze your study rhythms, pause behaviors, and peak hours as you record focus sessions.",
                    metricValue = "0 Sessions",
                    rationale = "Grounded analysis requires observed focus time.",
                    type = InsightType.OPPORTUNITY
                )
            )
            return insights
        }

        val activeDaysCount = getWeeklyFocusSummaries().count { it.totalMinutes >= 20 }
        insights.add(
            ProductivityInsight(
                title = "Study Rhythm Consistency",
                description = if (activeDaysCount >= 4) {
                    "Strong habit retention with $activeDaysCount study days in the past week."
                } else {
                    "Session distribution is fragmented ($activeDaysCount/7 active days). Aim for 30m daily minimum."
                },
                metricValue = "$activeDaysCount / 7 Days",
                rationale = "Calculated from recorded sessions exceeding 20 minutes active duration.",
                type = if (activeDaysCount >= 4) InsightType.STRENGTH else InsightType.OPPORTUNITY
            )
        )

        val totalActiveSeconds = sessionsList.sumOf { it.actualDurationSeconds }
        val totalPausedSeconds = sessionsList.sumOf { it.pausedDurationSeconds }
        val pauseRatio = if (totalActiveSeconds > 0) (totalPausedSeconds.toFloat() / (totalActiveSeconds + totalPausedSeconds) * 100).toInt() else 0

        insights.add(
            ProductivityInsight(
                title = "Session Continuity & Pauses",
                description = if (pauseRatio < 15) {
                    "Exceptional deep work continuity. Less than $pauseRatio% of session time lost to pauses."
                } else {
                    "Frequent interruptions observed ($pauseRatio% paused time). Consider lowering planned block length."
                },
                metricValue = "$pauseRatio% Paused",
                rationale = "Ratio of paused duration to total recorded session duration.",
                type = if (pauseRatio < 15) InsightType.STRENGTH else InsightType.ALERT
            )
        )

        val acadMins = sessionsList.filter { it.category == SessionCategory.ACADEMIC }.sumOf { it.actualDurationMinutes }
        val skillMins = sessionsList.filter { it.category == SessionCategory.SKILL }.sumOf { it.actualDurationMinutes }
        val totalMins = acadMins + skillMins
        val acadPct = if (totalMins > 0) (acadMins.toFloat() / totalMins * 100).toInt() else 50

        insights.add(
            ProductivityInsight(
                title = "Academic vs Skill Allocation",
                description = "Your study time is currently allocated $acadPct% to academic curriculum and ${100 - acadPct}% to practical skills.",
                metricValue = "$acadPct% Academic",
                rationale = "Aggregated across all completed sessions in historical storage.",
                type = InsightType.OPPORTUNITY
            )
        )

        val plannedSessions = sessionsList.filter { it.plannedDurationMinutes != null && it.plannedDurationMinutes > 0 }
        if (plannedSessions.isNotEmpty()) {
            val adherenceAvg = (plannedSessions.sumOf {
                val ratio = it.actualDurationMinutes.toFloat() / it.plannedDurationMinutes!!
                (ratio * 100).toInt()
            } / plannedSessions.size)
            insights.add(
                ProductivityInsight(
                    title = "Goal Completion Precision",
                    description = if (adherenceAvg >= 90) {
                        "Accurate self-estimation: completing $adherenceAvg% of planned target durations."
                    } else {
                        "Sessions end early relative to target ($adherenceAvg% completion). Try shorter blocks."
                    },
                    metricValue = "$adherenceAvg% Target",
                    rationale = "Actual session duration compared directly to chosen planned duration.",
                    type = if (adherenceAvg >= 85) InsightType.STRENGTH else InsightType.ALERT
                )
            )
        }

        return insights
    }
}

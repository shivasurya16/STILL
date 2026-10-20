package com.example.data

enum class SessionCategory {
    ACADEMIC,
    SKILL
}

enum class SessionStatus {
    COMPLETED,
    CANCELLED
}

// --- SUBTOPIC & TOPIC ---
data class Subtopic(
    val id: String,
    val name: String
)

data class Topic(
    val id: String,
    val name: String,
    val description: String? = null,
    val subtopics: List<Subtopic> = emptyList(),
    val notes: String = ""
) {
    val hasSubtopics: Boolean get() = subtopics.isNotEmpty()

    // Total trackable items: if subtopics exist, each subtopic is a trackable item. If not, the topic itself is 1 trackable item.
    val trackableCount: Int get() = if (hasSubtopics) subtopics.size else 1
}

// --- ACADEMICS HIERARCHY: Subject -> Chapter -> Topic -> Optional Subtopic ---
data class Chapter(
    val id: String,
    val name: String,
    val description: String? = null,
    val topics: List<Topic> = emptyList()
) {
    val totalTrackableItems: Int get() = topics.sumOf { it.trackableCount }
}

data class Subject(
    val id: String,
    val name: String,
    val description: String? = null,
    val chapters: List<Chapter> = emptyList()
) {
    val totalTrackableItems: Int get() = chapters.sumOf { it.totalTrackableItems }
}

// --- SKILLS HIERARCHY: Skill -> Stage -> Topic -> Optional Subtopic ---
data class Stage(
    val id: String,
    val name: String,
    val description: String? = null,
    val topics: List<Topic> = emptyList()
) {
    val totalTrackableItems: Int get() = topics.sumOf { it.trackableCount }
}

data class Skill(
    val id: String,
    val name: String,
    val description: String? = null,
    val stages: List<Stage> = emptyList()
) {
    val totalTrackableItems: Int get() = stages.sumOf { it.totalTrackableItems }
}

// --- PROGRESS RECORD (Separated from curriculum structure) ---
data class ProgressRecord(
    val completed: Boolean = false,
    val completedAt: String? = null
)

// Computed Progress ViewModels for UI Display
data class ProgressSummary(
    val completedCount: Int,
    val totalCount: Int
) {
    val percentage: Float
        get() = if (totalCount == 0) 0f else (completedCount.toFloat() / totalCount).coerceIn(0f, 1f)

    val percentageInt: Int get() = (percentage * 100).toInt()
    val isComplete: Boolean get() = totalCount > 0 && completedCount == totalCount
}

// --- STUDY SESSION ENGINE ---
data class StudySession(
    val id: String,
    val category: SessionCategory,
    val targetId: String,               // Subject ID or Skill ID
    val targetName: String,             // Subject Name or Skill Name (snapshot)
    val chapterOrStageId: String? = null,
    val chapterOrStageName: String? = null,
    val topicId: String? = null,
    val topicName: String? = null,
    val subTargetName: String? = null,  // Backward compatibility & quick display
    val startTime: Long,
    val endTime: Long,
    val plannedDurationMinutes: Int? = null,
    val pausedDurationSeconds: Long = 0,
    val notes: String = "",
    val status: SessionStatus = SessionStatus.COMPLETED
) {
    val actualDurationSeconds: Long
        get() {
            val totalSpan = (endTime - startTime) / 1000
            val net = totalSpan - pausedDurationSeconds
            return if (net > 0) net else 0
        }

    val actualDurationMinutes: Int get() = (actualDurationSeconds / 60).toInt()
}

data class ActiveSession(
    val id: String,
    val category: SessionCategory,
    val targetId: String,
    val targetName: String,
    val chapterOrStageId: String? = null,
    val chapterOrStageName: String? = null,
    val topicId: String? = null,
    val topicName: String? = null,
    val subTargetName: String? = null,
    val plannedDurationMinutes: Int? = null,
    val startTime: Long,
    val isPaused: Boolean = false,
    val pauseStartTime: Long? = null,
    val totalPausedSeconds: Long = 0
) {
    fun getElapsedSeconds(now: Long = System.currentTimeMillis()): Long {
        val rawSpan = if (isPaused && pauseStartTime != null) {
            (pauseStartTime - startTime) / 1000
        } else {
            (now - startTime) / 1000
        }
        val net = rawSpan - totalPausedSeconds
        return if (net > 0) net else 0
    }
}

// --- DISTRACTION APPS & USER SETTINGS ---
data class DistractionApp(
    val id: String,
    val appName: String,
    val packageName: String,
    val category: String,
    val todayUsageMinutes: Int,
    val limitMinutes: Int,
    val isBlocked: Boolean,
    val iconName: String
)

data class UserSettings(
    val dailyFocusGoalMinutes: Int = 180,
    val breakReminderIntervalMinutes: Int = 45,
    val strictDistractionBlock: Boolean = true,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val studentName: String = ""
)

// --- SHARED JSON IMPORT / VALIDATION PREVIEW ---
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val type: String = "",
    val rootCount: Int = 0,
    val containerCount: Int = 0, // chapters or stages count
    val topicCount: Int = 0,
    val subtopicCount: Int = 0,
    val totalTrackableItems: Int = 0,
    val summaryHierarchy: List<String> = emptyList(),
    val parsedSubjects: List<Subject> = emptyList(),
    val parsedSkills: List<Skill> = emptyList(),
    val rawJson: String = ""
)

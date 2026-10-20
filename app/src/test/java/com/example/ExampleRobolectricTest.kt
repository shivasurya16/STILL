package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("STILL", appName)
    }

    @Test
    fun `canonical academics json parses and validates successfully`() {
        val result = CurriculumJsonParser.validateAndParse(SampleData.canonicalAcademicsJson)
        assertTrue("Validation errors: ${result.errors}", result.isValid)
        assertEquals("academics", result.type)
        assertEquals(1, result.rootCount)
        assertEquals(1, result.containerCount) // 1 chapter
        assertEquals(2, result.topicCount)     // limits, continuity
        assertEquals(2, result.subtopicCount)  // limit-definition, limit-laws
        assertEquals(3, result.totalTrackableItems) // 2 subtopics + 1 topic without subtopics = 3
    }

    @Test
    fun `canonical skills json parses and validates successfully`() {
        val result = CurriculumJsonParser.validateAndParse(SampleData.canonicalSkillsJson)
        assertTrue("Validation errors: ${result.errors}", result.isValid)
        assertEquals("skills", result.type)
        assertEquals(1, result.rootCount)
        assertEquals(1, result.containerCount) // 1 stage
        assertEquals(2, result.topicCount)     // html-basics, css-basics
        assertEquals(2, result.subtopicCount)  // html-elements, html-attributes
        assertEquals(3, result.totalTrackableItems) // 2 subtopics + 1 topic without subtopics = 3
    }

    @Test
    fun `duplicate ID detection rejects invalid curriculum`() {
        val duplicateIdJson = """
        {
          "version": 1,
          "type": "academics",
          "subjects": [
            {
              "id": "duplicate-id",
              "name": "Math",
              "chapters": [
                {
                  "id": "duplicate-id",
                  "name": "Chapter 1",
                  "topics": []
                }
              ]
            }
          ]
        }
        """.trimIndent()

        val result = CurriculumJsonParser.validateAndParse(duplicateIdJson)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Duplicate ID", ignoreCase = true) })
    }

    @Test
    fun `unsupported version rejects invalid curriculum`() {
        val badVersionJson = """
        {
          "version": 2,
          "type": "academics",
          "subjects": []
        }
        """.trimIndent()

        val result = CurriculumJsonParser.validateAndParse(badVersionJson)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Unsupported version", ignoreCase = true) })
    }

    @Test
    fun `progress calculation derives topic completion from subtopics`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = StillRepository(context)

        val topics = listOf(
            Topic(
                id = "topic-with-sub",
                name = "Topic 1",
                subtopics = listOf(
                    Subtopic("sub-1", "Sub 1"),
                    Subtopic("sub-2", "Sub 2")
                )
            ),
            Topic(
                id = "topic-direct",
                name = "Topic 2",
                subtopics = emptyList()
            )
        )

        // Empty progress
        var progress = mapOf<String, ProgressRecord>()
        assertFalse(repo.isTopicComplete(topics[0], progress))
        assertFalse(repo.isTopicComplete(topics[1], progress))

        // Complete only 1 subtopic
        progress = mapOf("sub-1" to ProgressRecord(completed = true))
        assertFalse(repo.isTopicComplete(topics[0], progress))
        assertEquals(1, repo.getTopicProgressSummary(topics[0], progress).completedCount)
        assertEquals(2, repo.getTopicProgressSummary(topics[0], progress).totalCount)

        // Complete both subtopics
        progress = mapOf(
            "sub-1" to ProgressRecord(completed = true),
            "sub-2" to ProgressRecord(completed = true)
        )
        assertTrue(repo.isTopicComplete(topics[0], progress))

        // Complete direct topic
        progress = mapOf("topic-direct" to ProgressRecord(completed = true))
        assertTrue(repo.isTopicComplete(topics[1], progress))
    }

    @Test
    fun `active session timestamp math excludes pauses`() {
        val startTime = 1000_000L
        val pauseStartTime = 1060_000L
        val activeSession = ActiveSession(
            id = "test_sess",
            category = SessionCategory.ACADEMIC,
            targetId = "sub_math",
            targetName = "Math",
            plannedDurationMinutes = 45,
            startTime = startTime,
            isPaused = true,
            pauseStartTime = pauseStartTime,
            totalPausedSeconds = 10L
        )

        val elapsed = activeSession.getElapsedSeconds(now = 1200_000L)
        // In paused state: elapsed is (pauseStartTime - startTime)/1000 - totalPausedSeconds = 60 - 10 = 50s
        assertEquals(50L, elapsed)
    }

    @Test
    fun `curriculum export round trip maintains fidelity`() {
        val subjects = SampleData.getCanonicalSubjects()
        val exportedJson = CurriculumJsonParser.exportAcademicsJson(subjects)
        val revalidated = CurriculumJsonParser.validateAndParse(exportedJson)

        assertTrue("Exported JSON should validate cleanly: ${revalidated.errors}", revalidated.isValid)
        assertEquals(subjects.size, revalidated.parsedSubjects.size)
        assertEquals(subjects[0].id, revalidated.parsedSubjects[0].id)
        assertEquals(subjects[0].totalTrackableItems, revalidated.parsedSubjects[0].totalTrackableItems)

        val skills = SampleData.getCanonicalSkills()
        val exportedSkillsJson = CurriculumJsonParser.exportSkillsJson(skills)
        val revalidatedSkills = CurriculumJsonParser.validateAndParse(exportedSkillsJson)

        assertTrue("Exported Skills JSON should validate cleanly: ${revalidatedSkills.errors}", revalidatedSkills.isValid)
        assertEquals(skills.size, revalidatedSkills.parsedSkills.size)
        assertEquals(skills[0].id, revalidatedSkills.parsedSkills[0].id)
    }

    @Test
    fun `combined backup json exports and validates properly`() {
        val subjects = SampleData.getCanonicalSubjects()
        val skills = SampleData.getCanonicalSkills()
        val backupJson = CurriculumJsonParser.exportFullBackup(
            subjects = subjects,
            skills = skills,
            academicsProgress = emptyMap(),
            skillsProgress = emptyMap(),
            sessions = emptyList()
        )

        val result = CurriculumJsonParser.validateAndParse(backupJson)
        assertTrue("Backup JSON must validate: ${result.errors}", result.isValid)
        assertEquals("still_backup", result.type)
        assertEquals(subjects.size + skills.size, result.rootCount)
    }

    @Test
    fun `repository applyImport supports merge and replace`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = StillRepository(context)

        val parsedResult = CurriculumJsonParser.validateAndParse(SampleData.canonicalAcademicsJson)
        assertTrue(parsedResult.isValid)

        // Apply merge
        repo.applyImport(parsedResult, replaceExisting = false)
        val subjectsAfterMerge = repo.getSubjects()
        assertTrue(subjectsAfterMerge.isNotEmpty())
        assertEquals("mathematics", subjectsAfterMerge.first().id)

        // Apply replace
        repo.applyImport(parsedResult, replaceExisting = true)
        val subjectsAfterReplace = repo.getSubjects()
        assertEquals(1, subjectsAfterReplace.size)
        assertEquals("mathematics", subjectsAfterReplace.first().id)
    }

    @Test
    fun `profile name setup saves and trims name properly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = StillRepository(context)

        repo.clearUserName()
        assertFalse(repo.hasUserName())
        assertNull(repo.getUserName())

        // Save with whitespace
        repo.saveUserName("   Alex Rivera   ")
        assertTrue(repo.hasUserName())
        assertEquals("Alex Rivera", repo.getUserName())
        assertEquals("Alex Rivera", repo.getSettings().studentName)
    }

    @Test
    fun `profile name editing preserves curriculum and session data`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = StillRepository(context)

        // Seed initial data
        repo.loadCanonicalDemoData()
        repo.saveUserName("Initial Name")
        val initialSubjects = repo.getSubjects()
        val initialSessions = repo.getSessions()

        assertTrue(initialSubjects.isNotEmpty())

        // Edit name
        repo.saveUserName("Updated Name")
        assertEquals("Updated Name", repo.getUserName())

        // Verify curriculum and sessions were not reset or modified
        val currentSubjects = repo.getSubjects()
        val currentSessions = repo.getSessions()
        assertEquals(initialSubjects.size, currentSubjects.size)
        assertEquals(initialSessions.size, currentSessions.size)
    }

    @Test
    fun `clearAllData clears profile name and curriculum`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = StillRepository(context)

        repo.loadCanonicalDemoData()
        repo.saveUserName("Test Student")
        assertTrue(repo.hasUserName())
        assertTrue(repo.getSubjects().isNotEmpty())

        repo.clearAllData()
        assertFalse(repo.hasUserName())
        assertNull(repo.getUserName())
        assertTrue(repo.getSubjects().isEmpty())
    }
}

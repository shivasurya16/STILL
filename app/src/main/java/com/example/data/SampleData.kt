package com.example.data

object SampleData {

    val canonicalAcademicsJson: String = """
    {
      "version": 1,
      "type": "academics",
      "subjects": [
        {
          "id": "mathematics",
          "name": "Mathematics",
          "description": "Core mathematics curriculum",
          "chapters": [
            {
              "id": "differential-calculus",
              "name": "Differential Calculus",
              "description": "Limits, continuity, and derivatives",
              "topics": [
                {
                  "id": "limits",
                  "name": "Limits",
                  "description": "Understand limits and their properties",
                  "subtopics": [
                    {
                      "id": "limit-definition",
                      "name": "Definition of a Limit"
                    },
                    {
                      "id": "limit-laws",
                      "name": "Laws of Limits"
                    }
                  ]
                },
                {
                  "id": "continuity",
                  "name": "Continuity",
                  "description": "Continuity of functions",
                  "subtopics": []
                }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()

    val canonicalSkillsJson: String = """
    {
      "version": 1,
      "type": "skills",
      "skills": [
        {
          "id": "web-development",
          "name": "Web Development",
          "description": "Build modern websites and web applications",
          "stages": [
            {
              "id": "html-css",
              "name": "HTML and CSS",
              "description": "Learn the foundations of web development",
              "topics": [
                {
                  "id": "html-basics",
                  "name": "HTML Basics",
                  "description": "Elements, tags, attributes, and document structure",
                  "subtopics": [
                    {
                      "id": "html-elements",
                      "name": "HTML Elements"
                    },
                    {
                      "id": "html-attributes",
                      "name": "HTML Attributes"
                    }
                  ]
                },
                {
                  "id": "css-basics",
                  "name": "CSS Basics",
                  "description": "Selectors, properties, and styling",
                  "subtopics": []
                }
              ]
            }
          ]
        }
      ]
    }
    """.trimIndent()

    fun getCanonicalSubjects(): List<Subject> {
        val result = CurriculumJsonParser.validateAndParse(canonicalAcademicsJson)
        return result.parsedSubjects
    }

    fun getCanonicalSkills(): List<Skill> {
        val result = CurriculumJsonParser.validateAndParse(canonicalSkillsJson)
        return result.parsedSkills
    }

    fun getSampleDistractionApps(): List<DistractionApp> {
        return listOf(
            DistractionApp(
                id = "app_instagram",
                appName = "Instagram",
                packageName = "com.instagram.android",
                category = "Social Media",
                todayUsageMinutes = 35,
                limitMinutes = 45,
                isBlocked = true,
                iconName = "camera"
            ),
            DistractionApp(
                id = "app_youtube",
                appName = "YouTube",
                packageName = "com.google.android.youtube",
                category = "Entertainment",
                todayUsageMinutes = 42,
                limitMinutes = 60,
                isBlocked = false,
                iconName = "video"
            ),
            DistractionApp(
                id = "app_tiktok",
                appName = "TikTok",
                packageName = "com.zhiliaoapp.musically",
                category = "Social Media",
                todayUsageMinutes = 20,
                limitMinutes = 30,
                isBlocked = true,
                iconName = "music"
            ),
            DistractionApp(
                id = "app_reddit",
                appName = "Reddit",
                packageName = "com.reddit.frontpage",
                category = "Forums & News",
                todayUsageMinutes = 15,
                limitMinutes = 20,
                isBlocked = true,
                iconName = "forum"
            )
        )
    }

    fun getSampleSessions(): List<StudySession> {
        val now = System.currentTimeMillis()
        val oneHour = 3600_000L
        val oneDay = 86400_000L

        return listOf(
            StudySession(
                id = "sess_canonical_1",
                category = SessionCategory.ACADEMIC,
                targetId = "mathematics",
                targetName = "Mathematics",
                chapterOrStageId = "differential-calculus",
                chapterOrStageName = "Differential Calculus",
                topicId = "limits",
                topicName = "Limits",
                subTargetName = "Differential Calculus · Limits",
                startTime = now - (3 * oneHour),
                endTime = now - (2 * oneHour),
                plannedDurationMinutes = 60,
                pausedDurationSeconds = 120,
                notes = "Studied epsilon-delta definition and standard limits.",
                status = SessionStatus.COMPLETED
            ),
            StudySession(
                id = "sess_canonical_2",
                category = SessionCategory.SKILL,
                targetId = "web-development",
                targetName = "Web Development",
                chapterOrStageId = "html-css",
                chapterOrStageName = "HTML and CSS",
                topicId = "html-basics",
                topicName = "HTML Basics",
                subTargetName = "HTML and CSS · HTML Basics",
                startTime = now - (90 * 60_000L),
                endTime = now - (45 * 60_000L),
                plannedDurationMinutes = 45,
                pausedDurationSeconds = 60,
                notes = "Practiced semantic tags and attributes structure.",
                status = SessionStatus.COMPLETED
            ),
            StudySession(
                id = "sess_canonical_3",
                category = SessionCategory.ACADEMIC,
                targetId = "mathematics",
                targetName = "Mathematics",
                chapterOrStageId = "differential-calculus",
                chapterOrStageName = "Differential Calculus",
                topicId = "continuity",
                topicName = "Continuity",
                subTargetName = "Differential Calculus · Continuity",
                startTime = now - oneDay - (2 * oneHour),
                endTime = now - oneDay - (1 * oneHour),
                plannedDurationMinutes = 60,
                pausedDurationSeconds = 180,
                notes = "Intermediate value theorem proofs and problems.",
                status = SessionStatus.COMPLETED
            )
        )
    }
}

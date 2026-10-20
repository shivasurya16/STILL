package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.StillTokens

@Composable
fun TopicChecklistScreen(viewModel: MainViewModel) {
    BackHandler { viewModel.navigateBack() }

    val subjects by viewModel.subjects.collectAsState()
    val academicsProgress by viewModel.academicsProgress.collectAsState()
    val subjectId by viewModel.selectedSubjectId.collectAsState()
    val chapterId by viewModel.selectedChapterId.collectAsState()

    val subject = subjects.find { it.id == subjectId }
    val chapter = subject?.chapters?.find { it.id == chapterId }

    if (subject == null || chapter == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(StillTokens.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Chapter not found or curriculum was reloaded", color = StillTokens.textSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { viewModel.navigateBack() }) {
                    Text("Return")
                }
            }
        }
        return
    }

    val chapterSummary = viewModel.getChapterProgress(chapter)

    Scaffold(
        topBar = {
            StillTopAppBar(
                title = chapter.name,
                subtitle = "${subject.name} · ${chapterSummary.completedCount}/${chapterSummary.totalCount} Done",
                onBack = { viewModel.navigateBack() }
            )
        },
        containerColor = StillTokens.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Chapter Progress Banner
            item {
                StillCard(backgroundColor = StillTokens.elevated) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CHAPTER CHECKLIST",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = StillTokens.textSecondary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${chapterSummary.completedCount} of ${chapterSummary.totalCount} Trackable Items Completed",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = StillTokens.textPrimary
                                )
                            )
                        }

                        Text(
                            text = "${chapterSummary.percentageInt}%",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = if (chapterSummary.isComplete) StillTokens.focus else StillTokens.academics
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    StillProgressBar(
                        progress = chapterSummary.percentage,
                        color = if (chapterSummary.isComplete) StillTokens.focus else StillTokens.academics
                    )

                    chapter.description?.let { desc ->
                        if (desc.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                            )
                        }
                    }
                }
            }

            // Topics list in imported order
            if (chapter.topics.isEmpty()) {
                item {
                    StillCard {
                        Text(
                            text = "No topics exist under this chapter in the imported JSON curriculum.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = StillTokens.textSecondary)
                        )
                    }
                }
            } else {
                items(chapter.topics, key = { it.id }) { topic ->
                    val isTopicComplete = viewModel.isTopicComplete(topic)
                    val topicSummary = viewModel.getTopicProgress(topic)

                    StillCard(
                        backgroundColor = if (isTopicComplete) StillTokens.card.copy(alpha = 0.65f) else StillTokens.card
                    ) {
                        Column {
                            // Topic Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!topic.hasSubtopics) {
                                        // Direct trackable topic checkbox
                                        val isChecked = academicsProgress[topic.id]?.completed == true
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = {
                                                viewModel.toggleAcademicItem(topic.id)
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = StillTokens.focus,
                                                uncheckedColor = StillTokens.textSecondary
                                            ),
                                            modifier = Modifier.testTag("academic_topic_check_${topic.id}")
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    } else {
                                        // Derived status icon
                                        Icon(
                                            imageVector = if (isTopicComplete) Icons.Default.CheckCircle else Icons.Default.Adjust,
                                            contentDescription = null,
                                            tint = if (isTopicComplete) StillTokens.focus else StillTokens.academics,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                    }

                                    Column {
                                        Text(
                                            text = topic.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = if (isTopicComplete) FontWeight.Normal else FontWeight.Bold,
                                                color = if (isTopicComplete) StillTokens.textSecondary else StillTokens.textPrimary
                                            )
                                        )

                                        topic.description?.let { desc ->
                                            if (desc.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = desc,
                                                    style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (topic.hasSubtopics) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isTopicComplete) StillTokens.focus.copy(alpha = 0.15f)
                                                else StillTokens.elevated
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${topicSummary.completedCount}/${topicSummary.totalCount} Subtopics",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isTopicComplete) StillTokens.focus else StillTokens.textSecondary
                                            )
                                        )
                                    }
                                }
                            }

                            // If topic has subtopics, render nested checklist in imported order
                            if (topic.hasSubtopics) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    topic.subtopics.forEach { subtopic ->
                                        val isSubDone = academicsProgress[subtopic.id]?.completed == true
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(StillTokens.elevated)
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = isSubDone,
                                                    onCheckedChange = {
                                                        viewModel.toggleAcademicItem(subtopic.id)
                                                    },
                                                    colors = CheckboxDefaults.colors(
                                                        checkedColor = StillTokens.focus,
                                                        uncheckedColor = StillTokens.textSecondary
                                                    ),
                                                    modifier = Modifier.testTag("academic_subtopic_check_${subtopic.id}")
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = subtopic.name,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = if (isSubDone) StillTokens.textSecondary else StillTokens.textPrimary,
                                                        fontWeight = if (isSubDone) FontWeight.Normal else FontWeight.Medium
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.data.Stage
import com.example.ui.components.*
import com.example.ui.theme.StillTokens

@Composable
fun SkillProgressionScreen(viewModel: MainViewModel) {
    BackHandler { viewModel.navigateBack() }

    val skills by viewModel.skills.collectAsState()
    val skillsProgress by viewModel.skillsProgress.collectAsState()
    val skillId by viewModel.selectedSkillId.collectAsState()
    val skill = skills.find { it.id == skillId }

    // Track which stage cards are expanded (default first stage expanded)
    val expandedStages = remember(skillId) { mutableStateMapOf<String, Boolean>() }

    if (skill == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(StillTokens.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Skill learning path not found or updated", color = StillTokens.textSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { viewModel.navigateBack() }) {
                    Text("Return to Skills")
                }
            }
        }
        return
    }

    val overallSummary = viewModel.getSkillProgress(skill)

    Scaffold(
        topBar = {
            StillTopAppBar(
                title = skill.name,
                subtitle = "Skill Progression · ${skill.stages.size} Stages",
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
            // Skill Hero Progress Card
            item {
                StillCard(
                    backgroundColor = StillTokens.elevated,
                    borderColor = StillTokens.skills.copy(alpha = 0.4f)
                ) {
                    Text(
                        text = "SKILL PATH MASTERY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = StillTokens.skills
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${overallSummary.percentageInt}% Mastered",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = if (overallSummary.isComplete) StillTokens.focus else StillTokens.skills
                            )
                        )

                        Text(
                            text = "${overallSummary.completedCount} / ${overallSummary.totalCount} Topics Done",
                            style = MaterialTheme.typography.bodyMedium.copy(color = StillTokens.textSecondary)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    StillProgressBar(
                        progress = overallSummary.percentage,
                        color = if (overallSummary.isComplete) StillTokens.focus else StillTokens.skills,
                        height = 10.dp
                    )

                    skill.description?.let { desc ->
                        if (desc.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                            )
                        }
                    }
                }
            }

            // Stages Header
            item {
                SectionHeader(
                    title = "Progression Stages",
                    countLabel = "${skill.stages.size} Stages in Imported Order"
                )
            }

            if (skill.stages.isEmpty()) {
                item {
                    StillCard {
                        Text(
                            text = "This skill has no stages in the imported JSON curriculum.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = StillTokens.textSecondary)
                        )
                    }
                }
            } else {
                items(skill.stages, key = { it.id }) { stage ->
                    val stageSummary = viewModel.getStageProgress(stage)
                    val isExpanded = expandedStages[stage.id] ?: true

                    StillCard {
                        Column {
                            // Stage Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stage.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = StillTokens.textPrimary
                                        )
                                    )
                                    stage.description?.let { desc ->
                                        if (desc.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = desc,
                                                style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary),
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (stageSummary.isComplete) StillTokens.focus.copy(alpha = 0.15f)
                                                else StillTokens.skills.copy(alpha = 0.15f)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${stageSummary.percentageInt}%",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (stageSummary.isComplete) StillTokens.focus else StillTokens.skills
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(
                                        onClick = {
                                            expandedStages[stage.id] = !isExpanded
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                                            tint = StillTokens.textSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            StillProgressBar(
                                progress = stageSummary.percentage,
                                color = if (stageSummary.isComplete) StillTokens.focus else StillTokens.skills
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${stage.topics.size} Topics · ${stageSummary.completedCount}/${stageSummary.totalCount} Completed",
                                style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                            )

                            // Expandable Topics & Subtopics
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    HorizontalDivider(color = StillTokens.border)
                                    Spacer(modifier = Modifier.height(2.dp))

                                    stage.topics.forEach { topic ->
                                        val topicDone = viewModel.isTopicComplete(topic)
                                        val tSummary = viewModel.getTopicProgress(topic)

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(StillTokens.elevated)
                                                .padding(10.dp)
                                        ) {
                                            Column {
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
                                                            val isChecked = skillsProgress[topic.id]?.completed == true
                                                            Checkbox(
                                                                checked = isChecked,
                                                                onCheckedChange = {
                                                                    viewModel.toggleSkillItem(topic.id)
                                                                },
                                                                colors = CheckboxDefaults.colors(
                                                                    checkedColor = StillTokens.focus,
                                                                    uncheckedColor = StillTokens.textSecondary
                                                                ),
                                                                modifier = Modifier.testTag("skill_topic_check_${topic.id}")
                                                            )
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                        } else {
                                                            Icon(
                                                                imageVector = if (topicDone) Icons.Default.CheckCircle else Icons.Default.Adjust,
                                                                contentDescription = null,
                                                                tint = if (topicDone) StillTokens.focus else StillTokens.skills,
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                        }

                                                        Column {
                                                            Text(
                                                                text = topic.name,
                                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                                    color = if (topicDone) StillTokens.textSecondary else StillTokens.textPrimary,
                                                                    fontWeight = if (topicDone) FontWeight.Normal else FontWeight.SemiBold
                                                                )
                                                            )
                                                            topic.description?.let { desc ->
                                                                if (desc.isNotBlank()) {
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
                                                                .background(StillTokens.card)
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = "${tSummary.completedCount}/${tSummary.totalCount} subtopics",
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    color = if (topicDone) StillTokens.focus else StillTokens.skills,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            )
                                                        }
                                                    }
                                                }

                                                // If topic has subtopics
                                                if (topic.hasSubtopics) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(start = 16.dp),
                                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        topic.subtopics.forEach { subtopic ->
                                                            val isSubDone = skillsProgress[subtopic.id]?.completed == true
                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .background(StillTokens.card)
                                                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                                            ) {
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Checkbox(
                                                                        checked = isSubDone,
                                                                        onCheckedChange = {
                                                                            viewModel.toggleSkillItem(subtopic.id)
                                                                        },
                                                                        colors = CheckboxDefaults.colors(
                                                                            checkedColor = StillTokens.focus,
                                                                            uncheckedColor = StillTokens.textSecondary
                                                                        ),
                                                                        modifier = Modifier.testTag("skill_subtopic_check_${subtopic.id}")
                                                                    )
                                                                    Spacer(modifier = Modifier.width(6.dp))
                                                                    Text(
                                                                        text = subtopic.name,
                                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                                            color = if (isSubDone) StillTokens.textSecondary else StillTokens.textPrimary
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
                }
            }
        }
    }
}

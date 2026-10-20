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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.Chapter
import com.example.ui.components.*
import com.example.ui.theme.StillTokens

@Composable
fun SubjectDetailScreen(viewModel: MainViewModel) {
    BackHandler { viewModel.navigateBack() }

    val subjects by viewModel.subjects.collectAsState()
    val subjectId by viewModel.selectedSubjectId.collectAsState()
    val subject = subjects.find { it.id == subjectId }

    // Track expanded chapter cards
    val expandedChapters = remember { mutableStateMapOf<String, Boolean>() }

    if (subject == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(StillTokens.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Subject not found or was updated", color = StillTokens.textSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { viewModel.navigateBack() }) {
                    Text("Return to Academics")
                }
            }
        }
        return
    }

    val subjectSummary = viewModel.getSubjectProgress(subject)

    Scaffold(
        topBar = {
            StillTopAppBar(
                title = subject.name,
                subtitle = "Subject Curriculum · ${subject.chapters.size} Chapters",
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
            // Subject Summary Hero Card
            item {
                StillCard(backgroundColor = StillTokens.elevated) {
                    Text(
                        text = "ACADEMIC CURRICULUM MASTERY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = StillTokens.textSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${subjectSummary.percentageInt}% Complete",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = if (subjectSummary.isComplete) StillTokens.focus else StillTokens.academics
                            )
                        )
                        Text(
                            text = "${subjectSummary.completedCount} / ${subjectSummary.totalCount} Trackable Items",
                            style = MaterialTheme.typography.bodyMedium.copy(color = StillTokens.textSecondary)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    StillProgressBar(
                        progress = subjectSummary.percentage,
                        color = if (subjectSummary.isComplete) StillTokens.focus else StillTokens.academics,
                        height = 10.dp
                    )

                    subject.description?.let { desc ->
                        if (desc.isNotBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                            )
                        }
                    }
                }
            }

            // Chapters Header
            item {
                SectionHeader(
                    title = "Chapters & Modules",
                    countLabel = "${subject.chapters.size} Modules in Imported Order"
                )
            }

            if (subject.chapters.isEmpty()) {
                item {
                    StillCard {
                        Text(
                            text = "This subject contains no chapters in the imported JSON.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = StillTokens.textSecondary)
                        )
                    }
                }
            } else {
                items(subject.chapters, key = { it.id }) { chapter ->
                    val chapSummary = viewModel.getChapterProgress(chapter)
                    val isExpanded = expandedChapters[chapter.id] ?: false

                    StillCard {
                        Column {
                            // Chapter Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = chapter.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = StillTokens.textPrimary
                                        )
                                    )
                                    chapter.description?.let { desc ->
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
                                                if (chapSummary.isComplete) StillTokens.focus.copy(alpha = 0.15f)
                                                else StillTokens.academics.copy(alpha = 0.15f)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${chapSummary.percentageInt}%",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (chapSummary.isComplete) StillTokens.focus else StillTokens.academics
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(
                                        onClick = {
                                            expandedChapters[chapter.id] = !isExpanded
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
                                progress = chapSummary.percentage,
                                color = if (chapSummary.isComplete) StillTokens.focus else StillTokens.academics
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${chapter.topics.size} Topics · ${chapSummary.completedCount}/${chapSummary.totalCount} Trackable Items",
                                    style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                                )

                                TextButton(
                                    onClick = { viewModel.openTopicChecklist(subject.id, chapter.id) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Open Checklist", color = StillTokens.academics, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = StillTokens.academics, modifier = Modifier.size(14.dp))
                                }
                            }

                            // Expandable Topic List Preview
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    HorizontalDivider(color = StillTokens.border)
                                    Spacer(modifier = Modifier.height(4.dp))

                                    chapter.topics.forEach { topic ->
                                        val topicDone = viewModel.isTopicComplete(topic)
                                        val tSummary = viewModel.getTopicProgress(topic)

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(StillTokens.background)
                                                .clickable { viewModel.openTopicChecklist(subject.id, chapter.id) }
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    modifier = Modifier.weight(1f),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = if (topicDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                        contentDescription = null,
                                                        tint = if (topicDone) StillTokens.focus else StillTokens.textSecondary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = topic.name,
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                color = if (topicDone) StillTokens.textSecondary else StillTokens.textPrimary,
                                                                fontWeight = if (topicDone) FontWeight.Normal else FontWeight.SemiBold
                                                            )
                                                        )
                                                        if (topic.hasSubtopics) {
                                                            Text(
                                                                text = "${tSummary.completedCount}/${tSummary.totalCount} subtopics done",
                                                                style = MaterialTheme.typography.labelSmall.copy(color = StillTokens.textSecondary)
                                                            )
                                                        }
                                                    }
                                                }

                                                Icon(
                                                    imageVector = Icons.Default.ChevronRight,
                                                    contentDescription = null,
                                                    tint = StillTokens.textSecondary.copy(alpha = 0.5f),
                                                    modifier = Modifier.size(14.dp)
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

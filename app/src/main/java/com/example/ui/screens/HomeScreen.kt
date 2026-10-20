package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import com.example.AppScreen
import com.example.MainViewModel
import com.example.data.Chapter
import com.example.data.Subject
import com.example.data.Topic
import com.example.ui.components.*
import com.example.ui.theme.StillTokens

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onOpenImport: () -> Unit
) {
    val subjects by viewModel.subjects.collectAsState()
    val skills by viewModel.skills.collectAsState()
    val academicsProgress by viewModel.academicsProgress.collectAsState()
    val skillsProgress by viewModel.skillsProgress.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val activeElapsed by viewModel.activeSessionElapsedSeconds.collectAsState()
    val distractionApps by viewModel.distractionApps.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val userName by viewModel.userName.collectAsState()
    val userGreeting = remember(userName) {
        if (userName.isNotBlank()) "Welcome back, $userName" else "Learn. Improve. Grow."
    }

    val todayMins = viewModel.getTodayFocusedMinutes()
    val acadSummary = viewModel.getOverallAcademicsProgress()
    val skillSummary = viewModel.getOverallSkillsProgress()

    val totalDistractionMins = distractionApps.sumOf { it.todayUsageMinutes }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StillTokens.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header & Personalized Welcome
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "STILL",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = StillTokens.textPrimary
                        )
                    )
                    Text(
                        text = userGreeting,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = StillTokens.primary
                        )
                    )
                }

                if (subjects.isEmpty() && skills.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StillTokens.primary.copy(alpha = 0.15f))
                            .border(1.dp, StillTokens.primary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { onOpenImport() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Import JSON Curriculum",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = StillTokens.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // 2. Active Session Card (if session is in progress)
        if (activeSession != null) {
            item {
                val s = activeSession!!
                val minutes = activeElapsed / 60
                val seconds = activeElapsed % 60
                val timeString = String.format("%02d:%02d", minutes, seconds)

                StillCard(
                    backgroundColor = StillTokens.elevated,
                    borderColor = StillTokens.focus,
                    cornerRadius = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (s.isPaused) StillTokens.distraction else StillTokens.focus)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (s.isPaused) "SESSION PAUSED" else "ACTIVE FOCUS SESSION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = if (s.isPaused) StillTokens.distraction else StillTokens.focus
                                )
                            )
                        }

                        Text(
                            text = timeString,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = StillTokens.textPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = s.targetName + (s.subTargetName?.let { " · $it" } ?: ""),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = StillTokens.textPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (s.isPaused) viewModel.resumeSession() else viewModel.pauseSession()
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (s.isPaused) StillTokens.focus else StillTokens.elevated,
                                contentColor = if (s.isPaused) Color.Black else StillTokens.textPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = if (s.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (s.isPaused) "Resume" else "Pause", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.finishSession() },
                            modifier = Modifier.weight(1f).height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StillTokens.primary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Complete", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.cancelSession() },
                            modifier = Modifier.height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StillTokens.error)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 3. Today's Key Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Focus Time Card
                val hours = todayMins / 60
                val remainingMins = todayMins % 60
                val focusTimeLabel = if (hours > 0) "${hours}h ${remainingMins}m" else "${remainingMins}m"

                MetricCard(
                    title = "Today's Focus",
                    value = focusTimeLabel,
                    subtitle = "Goal: ${settings.dailyFocusGoalMinutes / 60}h (${(todayMins.toFloat() / settings.dailyFocusGoalMinutes * 100).toInt().coerceAtMost(100)}%)",
                    icon = Icons.Default.Timer,
                    accentColor = StillTokens.focus,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.FOCUS_TRACKING) }
                )

                // Distractions
                MetricCard(
                    title = "Distractions",
                    value = "${totalDistractionMins}m",
                    subtitle = "${distractionApps.count { it.isBlocked }} apps shielded",
                    icon = Icons.Default.Shield,
                    accentColor = StillTokens.distraction,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.DISTRACTION_MONITORING) }
                )
            }
        }

        // 4. Academic & Skill Progress Breakdown Card
        item {
            StillCard {
                Text(
                    text = "CURRICULUM & SKILL MASTERY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = StillTokens.textSecondary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Academic Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(StillTokens.academics.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = StillTokens.academics, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Academic Progress", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = StillTokens.textPrimary))
                            Text("${acadSummary.completedCount} of ${acadSummary.totalCount} trackable items done", style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary))
                        }
                    }
                    Text(
                        text = "${acadSummary.percentageInt}%",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = StillTokens.academics)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                StillProgressBar(progress = acadSummary.percentage, color = StillTokens.academics)

                Spacer(modifier = Modifier.height(20.dp))

                // Skills Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(StillTokens.skills.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = StillTokens.skills, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Skill Progress", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = StillTokens.textPrimary))
                            Text("${skillSummary.completedCount} of ${skillSummary.totalCount} trackable topics done", style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary))
                        }
                    }
                    Text(
                        text = "${skillSummary.percentageInt}%",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = StillTokens.skills)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                StillProgressBar(progress = skillSummary.percentage, color = StillTokens.skills)
            }
        }

        // 5. Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.STUDY_SESSION) },
                    modifier = Modifier.weight(1.2f).height(48.dp).testTag("home_start_session_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = StillTokens.primary, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start Focus", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOpenImport,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("home_import_json_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StillTokens.textPrimary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(StillTokens.border)
                    )
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import JSON")
                }
            }
        }

        // 6. Action Items / Next Incomplete Topics
        item {
            SectionHeader(
                title = "Next Incomplete Items",
                countLabel = "Action Items"
            )

            // Extract up to 4 incomplete items across subjects
            val pendingItems = mutableListOf<PendingActionItem>()

            subjects.forEach { sub ->
                sub.chapters.forEach { chap ->
                    chap.topics.forEach { top ->
                        if (top.hasSubtopics) {
                            top.subtopics.forEach { subtop ->
                                if (academicsProgress[subtop.id]?.completed != true && pendingItems.size < 4) {
                                    pendingItems.add(
                                        PendingActionItem(
                                            id = subtop.id,
                                            title = subtop.name,
                                            subtitle = "${sub.name} · ${chap.name} · ${top.name}",
                                            isAcademic = true,
                                            subjectId = sub.id,
                                            chapterId = chap.id
                                        )
                                    )
                                }
                            }
                        } else {
                            if (academicsProgress[top.id]?.completed != true && pendingItems.size < 4) {
                                pendingItems.add(
                                    PendingActionItem(
                                        id = top.id,
                                        title = top.name,
                                        subtitle = "${sub.name} · ${chap.name}",
                                        isAcademic = true,
                                        subjectId = sub.id,
                                        chapterId = chap.id
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (pendingItems.isEmpty()) {
                StillCard {
                    Text(
                        text = if (subjects.isEmpty()) "No curriculum imported yet. Tap 'Import JSON' to load courses."
                        else "All scheduled items complete! Great work on mastering your curriculum.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = StillTokens.textSecondary)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    pendingItems.forEach { item ->
                        StillCard(
                            onClick = {
                                viewModel.openTopicChecklist(item.subjectId, item.chapterId)
                            }
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
                                    Checkbox(
                                        checked = false,
                                        onCheckedChange = {
                                            viewModel.toggleAcademicItem(item.id)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = StillTokens.focus,
                                            uncheckedColor = StillTokens.textSecondary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = StillTokens.textPrimary
                                            )
                                        )
                                        Text(
                                            text = item.subtitle,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = StillTokens.textSecondary
                                            )
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = StillTokens.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class PendingActionItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val isAcademic: Boolean,
    val subjectId: String,
    val chapterId: String
)

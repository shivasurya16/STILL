package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.SessionCategory
import com.example.ui.components.*
import com.example.ui.theme.StillTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySessionScreen(viewModel: MainViewModel) {
    val activeSession by viewModel.activeSession.collectAsState()
    val activeElapsed by viewModel.activeSessionElapsedSeconds.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val skills by viewModel.skills.collectAsState()

    var selectedCategory by remember { mutableStateOf(SessionCategory.ACADEMIC) }

    // Selection states
    var selectedTargetId by remember { mutableStateOf("") }
    var selectedContainerId by remember { mutableStateOf<String?>(null) }
    var selectedTopicId by remember { mutableStateOf<String?>(null) }
    var plannedDurationMinutes by remember { mutableStateOf(45) }

    var showFinishDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var sessionNotes by remember { mutableStateOf("") }

    // Auto-select first target if empty
    LaunchedEffect(selectedCategory, subjects, skills) {
        if (selectedCategory == SessionCategory.ACADEMIC) {
            val firstSub = subjects.firstOrNull()
            if (firstSub != null && (selectedTargetId.isEmpty() || subjects.none { it.id == selectedTargetId })) {
                selectedTargetId = firstSub.id
                selectedContainerId = firstSub.chapters.firstOrNull()?.id
                selectedTopicId = firstSub.chapters.firstOrNull()?.topics?.firstOrNull()?.id
            }
        } else {
            val firstSkill = skills.firstOrNull()
            if (firstSkill != null && (selectedTargetId.isEmpty() || skills.none { it.id == selectedTargetId })) {
                selectedTargetId = firstSkill.id
                selectedContainerId = firstSkill.stages.firstOrNull()?.id
                selectedTopicId = firstSkill.stages.firstOrNull()?.topics?.firstOrNull()?.id
            }
        }
    }

    Scaffold(
        topBar = {
            StillTopAppBar(
                title = "Study Session",
                subtitle = if (activeSession != null) "Focus Timer Active" else "Configure Study Session"
            )
        },
        containerColor = StillTokens.background
    ) { padding ->
        if (activeSession != null) {
            // Active Running Timer View
            val s = activeSession!!
            val minutes = activeElapsed / 60
            val seconds = activeElapsed % 60
            val hours = minutes / 60
            val displayMinutes = minutes % 60

            val formattedTime = if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, displayMinutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }

            val targetColor = if (s.category == SessionCategory.ACADEMIC) StillTokens.academics else StillTokens.skills
            val plannedSecs = (s.plannedDurationMinutes ?: 45) * 60f
            val timerProgress = (activeElapsed.toFloat() / plannedSecs).coerceIn(0f, 1f)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header badge
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(targetColor.copy(alpha = 0.15f))
                            .border(1.dp, targetColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (s.category == SessionCategory.ACADEMIC) "ACADEMIC FOCUS" else "SKILL DEEP WORK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = targetColor
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = s.targetName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = StillTokens.textPrimary
                        )
                    )

                    s.subTargetName?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium.copy(color = StillTokens.textSecondary)
                        )
                    }
                }

                // Center Circular Timer
                Box(
                    modifier = Modifier.size(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StillCircularProgress(
                        progress = timerProgress,
                        modifier = Modifier.fillMaxSize(),
                        color = if (s.isPaused) StillTokens.distraction else StillTokens.focus,
                        trackColor = StillTokens.elevated,
                        strokeWidth = 14.dp
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formattedTime,
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    color = StillTokens.textPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (s.isPaused) StillTokens.distraction.copy(alpha = 0.2f) else StillTokens.focus.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (s.isPaused) "PAUSED" else "FOCUSING",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (s.isPaused) StillTokens.distraction else StillTokens.focus
                                    )
                                )
                            }

                            s.plannedDurationMinutes?.let { planned ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Target: ${planned}m",
                                    style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                                )
                            }
                        }
                    }
                }

                // Controls
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (s.isPaused) viewModel.resumeSession() else viewModel.pauseSession()
                            },
                            modifier = Modifier.weight(1f).height(54.dp).testTag("session_pause_resume_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (s.isPaused) StillTokens.focus else StillTokens.elevated,
                                contentColor = if (s.isPaused) Color.Black else StillTokens.textPrimary
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = if (s.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (s.isPaused) "Resume" else "Pause", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showFinishDialog = true },
                            modifier = Modifier.weight(1f).height(54.dp).testTag("session_finish_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StillTokens.primary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Finish & Save", fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = { showCancelDialog = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("session_cancel_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StillTokens.error),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(StillTokens.error.copy(alpha = 0.5f))
                        )
                    ) {
                        Text("Discard Session")
                    }
                }
            }
        } else {
            // Configuration & Launch View
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Category Switcher
                item {
                    Text(
                        text = "1. FOCUS DOMAIN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = StillTokens.textSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Academic Option
                        StillCard(
                            modifier = Modifier.weight(1f),
                            backgroundColor = if (selectedCategory == SessionCategory.ACADEMIC) StillTokens.elevated else StillTokens.card,
                            borderColor = if (selectedCategory == SessionCategory.ACADEMIC) StillTokens.academics else StillTokens.border,
                            onClick = {
                                selectedCategory = SessionCategory.ACADEMIC
                                val sub = subjects.firstOrNull()
                                selectedTargetId = sub?.id ?: ""
                                selectedContainerId = sub?.chapters?.firstOrNull()?.id
                                selectedTopicId = sub?.chapters?.firstOrNull()?.topics?.firstOrNull()?.id
                            }
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = StillTokens.academics, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Academics", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = StillTokens.textPrimary))
                            Text("${subjects.size} Courses", style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary))
                        }

                        // Skills Option
                        StillCard(
                            modifier = Modifier.weight(1f),
                            backgroundColor = if (selectedCategory == SessionCategory.SKILL) StillTokens.elevated else StillTokens.card,
                            borderColor = if (selectedCategory == SessionCategory.SKILL) StillTokens.skills else StillTokens.border,
                            onClick = {
                                selectedCategory = SessionCategory.SKILL
                                val sk = skills.firstOrNull()
                                selectedTargetId = sk?.id ?: ""
                                selectedContainerId = sk?.stages?.firstOrNull()?.id
                                selectedTopicId = sk?.stages?.firstOrNull()?.topics?.firstOrNull()?.id
                            }
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = StillTokens.skills, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Skills", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = StillTokens.textPrimary))
                            Text("${skills.size} Paths", style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary))
                        }
                    }
                }

                // 2. Select Target & Hierarchy
                item {
                    Text(
                        text = "2. SELECT CURRICULUM HIERARCHY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = StillTokens.textSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedCategory == SessionCategory.ACADEMIC) {
                        if (subjects.isEmpty()) {
                            StillCard {
                                Text("No academic courses imported yet. Please import a curriculum in Academics.", color = StillTokens.textSecondary)
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Subject dropdown/selector
                                subjects.forEach { sub ->
                                    val isSelected = sub.id == selectedTargetId
                                    StillCard(
                                        backgroundColor = if (isSelected) StillTokens.elevated else StillTokens.card,
                                        borderColor = if (isSelected) StillTokens.academics else StillTokens.border,
                                        onClick = {
                                            selectedTargetId = sub.id
                                            selectedContainerId = sub.chapters.firstOrNull()?.id
                                            selectedTopicId = sub.chapters.firstOrNull()?.topics?.firstOrNull()?.id
                                        }
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(sub.name, fontWeight = FontWeight.Bold, color = StillTokens.textPrimary)
                                                if (isSelected) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StillTokens.academics)
                                                }
                                            }

                                            // If selected, show chapter and topic pickers
                                            if (isSelected && sub.chapters.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text("Chapter:", style = MaterialTheme.typography.labelSmall.copy(color = StillTokens.textSecondary))
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    sub.chapters.forEach { chap ->
                                                        val isChapSelected = chap.id == selectedContainerId
                                                        FilterChip(
                                                            selected = isChapSelected,
                                                            onClick = {
                                                                selectedContainerId = chap.id
                                                                selectedTopicId = chap.topics.firstOrNull()?.id
                                                            },
                                                            label = { Text(chap.name, fontSize = 11.sp) },
                                                            colors = FilterChipDefaults.filterChipColors(
                                                                selectedContainerColor = StillTokens.academics,
                                                                selectedLabelColor = Color.Black
                                                            )
                                                        )
                                                    }
                                                }

                                                val curChap = sub.chapters.find { it.id == selectedContainerId }
                                                if (curChap != null && curChap.topics.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text("Topic:", style = MaterialTheme.typography.labelSmall.copy(color = StillTokens.textSecondary))
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        curChap.topics.forEach { top ->
                                                            val isTopSelected = top.id == selectedTopicId
                                                            FilterChip(
                                                                selected = isTopSelected,
                                                                onClick = { selectedTopicId = top.id },
                                                                label = { Text(top.name, fontSize = 11.sp) },
                                                                colors = FilterChipDefaults.filterChipColors(
                                                                    selectedContainerColor = StillTokens.focus,
                                                                    selectedLabelColor = Color.Black
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
                    } else {
                        // Skills Selector
                        if (skills.isEmpty()) {
                            StillCard {
                                Text("No skills imported yet. Please import a skill curriculum in Skills.", color = StillTokens.textSecondary)
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                skills.forEach { sk ->
                                    val isSelected = sk.id == selectedTargetId
                                    StillCard(
                                        backgroundColor = if (isSelected) StillTokens.elevated else StillTokens.card,
                                        borderColor = if (isSelected) StillTokens.skills else StillTokens.border,
                                        onClick = {
                                            selectedTargetId = sk.id
                                            selectedContainerId = sk.stages.firstOrNull()?.id
                                            selectedTopicId = sk.stages.firstOrNull()?.topics?.firstOrNull()?.id
                                        }
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(sk.name, fontWeight = FontWeight.Bold, color = StillTokens.textPrimary)
                                                if (isSelected) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StillTokens.skills)
                                                }
                                            }

                                            if (isSelected && sk.stages.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text("Stage:", style = MaterialTheme.typography.labelSmall.copy(color = StillTokens.textSecondary))
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    sk.stages.forEach { st ->
                                                        val isStSelected = st.id == selectedContainerId
                                                        FilterChip(
                                                            selected = isStSelected,
                                                            onClick = {
                                                                selectedContainerId = st.id
                                                                selectedTopicId = st.topics.firstOrNull()?.id
                                                            },
                                                            label = { Text(st.name, fontSize = 11.sp) },
                                                            colors = FilterChipDefaults.filterChipColors(
                                                                selectedContainerColor = StillTokens.skills,
                                                                selectedLabelColor = Color.Black
                                                            )
                                                        )
                                                    }
                                                }

                                                val curStage = sk.stages.find { it.id == selectedContainerId }
                                                if (curStage != null && curStage.topics.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text("Topic:", style = MaterialTheme.typography.labelSmall.copy(color = StillTokens.textSecondary))
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        curStage.topics.forEach { top ->
                                                            val isTopSelected = top.id == selectedTopicId
                                                            FilterChip(
                                                                selected = isTopSelected,
                                                                onClick = { selectedTopicId = top.id },
                                                                label = { Text(top.name, fontSize = 11.sp) },
                                                                colors = FilterChipDefaults.filterChipColors(
                                                                    selectedContainerColor = StillTokens.focus,
                                                                    selectedLabelColor = Color.Black
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

                // 3. Planned Duration
                item {
                    Text(
                        text = "3. PLANNED SESSION DURATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = StillTokens.textSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val presetDurations = listOf(25, 45, 60, 90)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetDurations.forEach { mins ->
                            val isSelected = plannedDurationMinutes == mins
                            Button(
                                onClick = { plannedDurationMinutes = mins },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) StillTokens.primary else StillTokens.card,
                                    contentColor = if (isSelected) Color.White else StillTokens.textSecondary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) StillTokens.primary else StillTokens.border
                                )
                            ) {
                                Text("${mins}m", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 4. Launch Button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (selectedCategory == SessionCategory.ACADEMIC) {
                                val curSub = subjects.find { it.id == selectedTargetId } ?: subjects.firstOrNull()
                                val curChap = curSub?.chapters?.find { it.id == selectedContainerId }
                                val curTopic = curChap?.topics?.find { it.id == selectedTopicId }

                                viewModel.startSession(
                                    category = SessionCategory.ACADEMIC,
                                    targetId = curSub?.id ?: "academics",
                                    targetName = curSub?.name ?: "Academic Session",
                                    chapterOrStageId = curChap?.id,
                                    chapterOrStageName = curChap?.name,
                                    topicId = curTopic?.id,
                                    topicName = curTopic?.name,
                                    plannedMinutes = plannedDurationMinutes
                                )
                            } else {
                                val curSkill = skills.find { it.id == selectedTargetId } ?: skills.firstOrNull()
                                val curStage = curSkill?.stages?.find { it.id == selectedContainerId }
                                val curTopic = curStage?.topics?.find { it.id == selectedTopicId }

                                viewModel.startSession(
                                    category = SessionCategory.SKILL,
                                    targetId = curSkill?.id ?: "skills",
                                    targetName = curSkill?.name ?: "Skill Session",
                                    chapterOrStageId = curStage?.id,
                                    chapterOrStageName = curStage?.name,
                                    topicId = curTopic?.id,
                                    topicName = curTopic?.name,
                                    plannedMinutes = plannedDurationMinutes
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("launch_focus_session_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StillTokens.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start ${plannedDurationMinutes}m Focus Session", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }

    // Finish Session Dialog (Optional Notes)
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("Complete Session", color = StillTokens.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Great work! Add key takeaways or notes from this focus block:", color = StillTokens.textSecondary)
                    OutlinedTextField(
                        value = sessionNotes,
                        onValueChange = { sessionNotes = it },
                        label = { Text("Session Notes") },
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("session_notes_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StillTokens.primary,
                            unfocusedBorderColor = StillTokens.border
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.finishSession(sessionNotes.trim())
                        sessionNotes = ""
                        showFinishDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StillTokens.focus, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save to History", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text("Back", color = StillTokens.textSecondary)
                }
            },
            containerColor = StillTokens.card
        )
    }

    // Cancel Session Confirmation Dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Discard Active Session?", color = StillTokens.textPrimary) },
            text = {
                Text(
                    "Are you sure you want to cancel? The recorded timer will be discarded and not logged to your focus history.",
                    color = StillTokens.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelSession()
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StillTokens.error, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Focusing", color = StillTokens.textSecondary)
                }
            },
            containerColor = StillTokens.card
        )
    }
}

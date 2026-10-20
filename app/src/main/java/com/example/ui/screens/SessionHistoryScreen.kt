package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import com.example.data.SessionCategory
import com.example.data.StudySession
import com.example.ui.components.*
import com.example.ui.theme.StillTokens
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SessionHistoryScreen(viewModel: MainViewModel) {
    BackHandler { viewModel.navigateBack() }

    val sessions by viewModel.sessions.collectAsState()
    var selectedCategoryFilter by remember { mutableStateOf<SessionCategory?>(null) }
    var sessionToDelete by remember { mutableStateOf<StudySession?>(null) }

    val filteredSessions = remember(sessions, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) sessions
        else sessions.filter { it.category == selectedCategoryFilter }
    }

    val totalMinutes = filteredSessions.sumOf { it.actualDurationMinutes }
    val totalHours = totalMinutes / 60
    val remMins = totalMinutes % 60
    val totalTimeStr = if (totalHours > 0) "${totalHours}h ${remMins}m" else "${remMins}m"

    val dateFormat = remember { SimpleDateFormat("EEE, MMM d · h:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            StillTopAppBar(
                title = "Session History",
                subtitle = "${filteredSessions.size} Sessions Recorded",
                onBack = { viewModel.navigateBack() }
            )
        },
        containerColor = StillTokens.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Category Filter Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategoryFilter == null,
                    onClick = { selectedCategoryFilter = null },
                    label = { Text("All (${sessions.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StillTokens.primary,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = selectedCategoryFilter == SessionCategory.ACADEMIC,
                    onClick = { selectedCategoryFilter = SessionCategory.ACADEMIC },
                    label = { Text("Academics") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StillTokens.academics,
                        selectedLabelColor = Color.Black
                    )
                )

                FilterChip(
                    selected = selectedCategoryFilter == SessionCategory.SKILL,
                    onClick = { selectedCategoryFilter = SessionCategory.SKILL },
                    label = { Text("Skills") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StillTokens.skills,
                        selectedLabelColor = Color.Black
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Total Hours Card
            StillCard(backgroundColor = StillTokens.elevated) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL NET FOCUS TIME",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = StillTokens.textSecondary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = totalTimeStr,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = StillTokens.focus
                            )
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.HistoryEdu,
                        contentDescription = null,
                        tint = StillTokens.focus,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredSessions.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Timer,
                    title = "No Focus Sessions Yet",
                    description = "When you complete study blocks, your detailed chronological sessions will appear here.",
                    actionButtonText = "Launch Timer",
                    onActionClick = { viewModel.navigateTo(com.example.AppScreen.STUDY_SESSION) }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredSessions, key = { it.id }) { s ->
                        val isAcad = s.category == SessionCategory.ACADEMIC
                        val accentColor = if (isAcad) StillTokens.academics else StillTokens.skills

                        StillCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(accentColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isAcad) "ACADEMIC" else "SKILL",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = accentColor,
                                                    letterSpacing = 1.sp
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = dateFormat.format(Date(s.startTime)),
                                            style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = s.targetName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = StillTokens.textPrimary
                                        )
                                    )

                                    s.subTargetName?.let {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                                        )
                                    }

                                    if (s.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(StillTokens.elevated)
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                text = "“${s.notes}”",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = StillTokens.textPrimary,
                                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                                )
                                            )
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${s.actualDurationMinutes}m",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            color = StillTokens.textPrimary
                                        )
                                    )

                                    if (s.pausedDurationSeconds > 0) {
                                        Text(
                                            text = "${s.pausedDurationSeconds / 60}m paused",
                                            style = MaterialTheme.typography.labelSmall.copy(color = StillTokens.distraction)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    IconButton(
                                        onClick = { sessionToDelete = s },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete record",
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
    }

    // Delete Session Confirmation
    sessionToDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Delete Session Record?", color = StillTokens.textPrimary) },
            text = { Text("Remove this ${session.actualDurationMinutes}m focus session from your records?", color = StillTokens.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSession(session.id)
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StillTokens.error, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text("Cancel", color = StillTokens.textSecondary)
                }
            },
            containerColor = StillTokens.card
        )
    }
}

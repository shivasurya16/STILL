package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.data.SessionCategory
import com.example.ui.components.*
import com.example.ui.theme.StillTokens
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DailyOverviewScreen(viewModel: MainViewModel) {
    BackHandler { viewModel.navigateBack() }

    val selectedEpoch by viewModel.selectedOverviewDate.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val distractionApps by viewModel.distractionApps.collectAsState()

    val cal = remember(selectedEpoch) {
        Calendar.getInstance().apply { timeInMillis = selectedEpoch }
    }

    val dayStart = remember(selectedEpoch) {
        Calendar.getInstance().apply {
            timeInMillis = selectedEpoch
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val dayEnd = remember(selectedEpoch) {
        dayStart + 86400_000L - 1
    }

    val daySessions = remember(sessions, dayStart, dayEnd) {
        sessions.filter { it.startTime in dayStart..dayEnd }
    }

    val dayFocusMinutes = daySessions.sumOf { it.actualDurationMinutes }
    val dayGoalMinutes = settings.dailyFocusGoalMinutes
    val goalPct = (dayFocusMinutes.toFloat() / dayGoalMinutes * 100).toInt().coerceAtMost(100)

    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            StillTopAppBar(
                title = "Daily Overview",
                subtitle = "Date-Specific Performance",
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
            // Date Switcher Header
            item {
                StillCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.setOverviewDate(selectedEpoch - 86400_000L)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Day",
                                tint = StillTokens.textPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dateFormat.format(Date(selectedEpoch)),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = StillTokens.textPrimary
                                )
                            )
                            val isToday = dayStart <= System.currentTimeMillis() && System.currentTimeMillis() <= dayEnd
                            if (isToday) {
                                Text(
                                    text = "TODAY",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = StillTokens.focus,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                viewModel.setOverviewDate(selectedEpoch + 86400_000L)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Day",
                                tint = StillTokens.textPrimary
                            )
                        }
                    }
                }
            }

            // Daily Target Completion Card
            item {
                StillCard(backgroundColor = StillTokens.elevated) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "FOCUS GOAL PROGRESS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = StillTokens.textSecondary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${dayFocusMinutes}m / ${dayGoalMinutes}m",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = StillTokens.textPrimary
                                )
                            )
                        }

                        Text(
                            text = "$goalPct%",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = if (goalPct >= 100) StillTokens.focus else StillTokens.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    StillProgressBar(
                        progress = (goalPct / 100f).coerceIn(0f, 1f),
                        color = if (goalPct >= 100) StillTokens.focus else StillTokens.primary
                    )
                }
            }

            // Day Summary Metrics
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Sessions",
                        value = "${daySessions.size}",
                        subtitle = "${daySessions.sumOf { it.pausedDurationSeconds / 60 }}m paused",
                        icon = Icons.Default.Timer,
                        accentColor = StillTokens.primary,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Academics",
                        value = "${daySessions.filter { it.category == SessionCategory.ACADEMIC }.sumOf { it.actualDurationMinutes }}m",
                        subtitle = "Curriculum study",
                        icon = Icons.Default.MenuBook,
                        accentColor = StillTokens.academics,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Skills",
                        value = "${daySessions.filter { it.category == SessionCategory.SKILL }.sumOf { it.actualDurationMinutes }}m",
                        subtitle = "Practice time",
                        icon = Icons.Default.Psychology,
                        accentColor = StillTokens.skills,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Day Sessions List
            item {
                SectionHeader(
                    title = "Sessions on this Day",
                    countLabel = "${daySessions.size} Logged"
                )
            }

            if (daySessions.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.EventBusy,
                        title = "No Sessions on this Date",
                        description = "There were no recorded study sessions on this date.",
                        actionButtonText = "Launch Timer",
                        onActionClick = { viewModel.navigateTo(com.example.AppScreen.STUDY_SESSION) }
                    )
                }
            } else {
                items(daySessions, key = { it.id }) { s ->
                    val isAcad = s.category == SessionCategory.ACADEMIC
                    val accent = if (isAcad) StillTokens.academics else StillTokens.skills

                    StillCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(accent.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isAcad) "ACADEMIC" else "SKILL",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = accent
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = timeFormat.format(Date(s.startTime)),
                                        style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = s.targetName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = StillTokens.textPrimary
                                    )
                                )
                                s.subTargetName?.let {
                                    Text(text = it, style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary))
                                }
                            }

                            Text(
                                text = "${s.actualDurationMinutes}m",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = StillTokens.textPrimary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

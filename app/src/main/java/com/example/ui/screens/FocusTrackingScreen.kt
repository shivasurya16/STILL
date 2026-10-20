package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.StillTokens

@Composable
fun FocusTrackingScreen(viewModel: MainViewModel) {
    BackHandler { viewModel.navigateBack() }

    val weeklyData = remember { viewModel.getWeeklyFocusSummaries() }
    val sessions by viewModel.sessions.collectAsState()

    val totalWeekMins = weeklyData.sumOf { it.totalMinutes }
    val maxDayMins = (weeklyData.maxOfOrNull { it.totalMinutes } ?: 60).coerceAtLeast(60)

    val totalPausedMins = sessions.sumOf { it.pausedDurationSeconds / 60 }
    val plannedSessions = sessions.filter { it.plannedDurationMinutes != null && it.plannedDurationMinutes > 0 }
    val totalPlannedMins = plannedSessions.sumOf { it.plannedDurationMinutes ?: 0 }
    val totalActualMinsInPlanned = plannedSessions.sumOf { it.actualDurationMinutes }

    Scaffold(
        topBar = {
            StillTopAppBar(
                title = "Focus Tracking",
                subtitle = "Weekly Trends & Session Continuity",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Metric
            item {
                StillCard(backgroundColor = StillTokens.elevated) {
                    Text(
                        text = "PAST 7 DAYS FOCUS ACCUMULATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = StillTokens.textSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${totalWeekMins / 60}h ${totalWeekMins % 60}m",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = StillTokens.focus
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Daily average: ${(totalWeekMins / 7f).toInt()}m · Active days: ${weeklyData.count { it.totalMinutes > 0 }}/7",
                        style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                    )
                }
            }

            // Weekly Focus Bar Chart Card
            item {
                StillCard {
                    Text(
                        text = "WEEKLY STUDY TIME DISTRIBUTION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = StillTokens.textSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Responsive Bar Chart
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyData.forEach { day ->
                            val heightFraction = (day.totalMinutes.toFloat() / maxDayMins).coerceIn(0.05f, 1f)

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Text(
                                    text = if (day.totalMinutes > 0) "${day.totalMinutes}m" else "0",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        color = if (day.totalMinutes > 0) StillTokens.textPrimary else StillTokens.textSecondary.copy(alpha = 0.5f)
                                    )
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Stacked bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .fillMaxHeight(heightFraction)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(StillTokens.elevated)
                                ) {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        val acadFraction = if (day.totalMinutes > 0) day.academicMinutes.toFloat() / day.totalMinutes else 0f
                                        val skillFraction = if (day.totalMinutes > 0) day.skillMinutes.toFloat() / day.totalMinutes else 0f

                                        if (skillFraction > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .weight(skillFraction.coerceAtLeast(0.01f))
                                                    .background(StillTokens.skills)
                                            )
                                        }
                                        if (acadFraction > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .weight(acadFraction.coerceAtLeast(0.01f))
                                                    .background(StillTokens.academics)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = day.dayLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = StillTokens.textSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(StillTokens.academics))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Academics", style = MaterialTheme.typography.labelSmall.copy(color = StillTokens.textSecondary))

                        Spacer(modifier = Modifier.width(16.dp))

                        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(StillTokens.skills))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Skills", style = MaterialTheme.typography.labelSmall.copy(color = StillTokens.textSecondary))
                    }
                }
            }

            // Planned vs Actual Card
            item {
                StillCard {
                    Text(
                        text = "PLANNED VS ACTUAL TIME ADHERENCE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = StillTokens.textSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val adherencePct = if (totalPlannedMins > 0) {
                        (totalActualMinsInPlanned.toFloat() / totalPlannedMins * 100).toInt()
                    } else 100

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$adherencePct% Target Fulfilled",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = StillTokens.textPrimary
                                )
                            )
                            Text(
                                text = "${totalActualMinsInPlanned}m actual vs ${totalPlannedMins}m scheduled",
                                style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(StillTokens.focus.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${plannedSessions.size} Sessions",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = StillTokens.focus
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    StillProgressBar(
                        progress = (adherencePct / 100f).coerceIn(0f, 1f),
                        color = StillTokens.focus
                    )
                }
            }

            // Interruptions & Pauses Card
            item {
                StillCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "RECORDED INTERRUPTIONS & PAUSES",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = StillTokens.textSecondary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${totalPausedMins}m Paused Time",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = StillTokens.distraction
                                )
                            )
                            Text(
                                text = "Excluded strictly from net focused work duration",
                                style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.PauseCircleFilled,
                            contentDescription = null,
                            tint = StillTokens.distraction,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

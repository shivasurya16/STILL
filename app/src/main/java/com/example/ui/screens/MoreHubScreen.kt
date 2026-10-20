package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AppScreen
import com.example.MainViewModel
import com.example.ui.components.StillCard
import com.example.ui.components.StillTopAppBar
import com.example.ui.theme.StillTokens

data class HubMenuItem(
    val screen: AppScreen,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val badgeText: String? = null
)

@Composable
fun MoreHubScreen(
    viewModel: MainViewModel,
    onOpenImport: () -> Unit
) {
    val distractionApps by viewModel.distractionApps.collectAsState()
    val sessions by viewModel.sessions.collectAsState()

    val totalDistractionMins = distractionApps.sumOf { it.todayUsageMinutes }

    val menuItems = remember(distractionApps, sessions) {
        listOf(
            HubMenuItem(
                screen = AppScreen.DISTRACTION_MONITORING,
                title = "Distraction Defense",
                subtitle = "App limits, screen time & sandbox shield",
                icon = Icons.Default.Shield,
                accentColor = StillTokens.distraction,
                badgeText = "${totalDistractionMins}m today"
            ),
            HubMenuItem(
                screen = AppScreen.PRODUCTIVITY_INTELLIGENCE,
                title = "Productivity Intelligence",
                subtitle = "Rhythms, continuity & empirical suggestions",
                icon = Icons.Default.Insights,
                accentColor = StillTokens.primary
            ),
            HubMenuItem(
                screen = AppScreen.FOCUS_TRACKING,
                title = "Focus Tracking",
                subtitle = "Weekly distribution, planned vs actual time",
                icon = Icons.Default.BarChart,
                accentColor = StillTokens.focus
            ),
            HubMenuItem(
                screen = AppScreen.DAILY_OVERVIEW,
                title = "Daily Overview",
                subtitle = "Date-specific performance & milestones",
                icon = Icons.Default.CalendarToday,
                accentColor = StillTokens.academics
            ),
            HubMenuItem(
                screen = AppScreen.SESSION_HISTORY,
                title = "Session History",
                subtitle = "Detailed chronological records & takeaways",
                icon = Icons.Default.History,
                accentColor = StillTokens.textSecondary,
                badgeText = "${sessions.size} logged"
            ),
            HubMenuItem(
                screen = AppScreen.SETTINGS,
                title = "Settings & Data Management",
                subtitle = "Goals, JSON import/export, reset controls",
                icon = Icons.Default.Settings,
                accentColor = StillTokens.textSecondary
            )
        )
    }

    Scaffold(
        topBar = {
            StillTopAppBar(
                title = "More Tools",
                subtitle = "Analytics, Wellbeing & Settings"
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Import Syllabus Quick Card
            item {
                StillCard(
                    backgroundColor = StillTokens.elevated,
                    borderColor = StillTokens.academics.copy(alpha = 0.5f),
                    onClick = onOpenImport
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
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(StillTokens.academics.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.UploadFile,
                                    contentDescription = null,
                                    tint = StillTokens.academics,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = "Import Syllabus JSON",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = StillTokens.textPrimary
                                    )
                                )
                                Text(
                                    text = "Load external course structures directly",
                                    style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = StillTokens.academics
                        )
                    }
                }
            }

            // Menu Items List
            items(menuItems) { item ->
                StillCard(
                    onClick = { viewModel.navigateTo(item.screen) }
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
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(item.accentColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = item.accentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = StillTokens.textPrimary
                                    )
                                )
                                Text(
                                    text = item.subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (item.badgeText != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(StillTokens.elevated)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.badgeText,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = item.accentColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
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

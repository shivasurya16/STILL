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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.InsightType
import com.example.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.StillTokens

@Composable
fun ProductivityIntelligenceScreen(viewModel: MainViewModel) {
    BackHandler { viewModel.navigateBack() }

    val insights = remember { viewModel.getProductivityInsights() }
    val sessions by viewModel.sessions.collectAsState()

    Scaffold(
        topBar = {
            StillTopAppBar(
                title = "Productivity Intelligence",
                subtitle = "Empirical Rhythms & Actionable Recommendations",
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
            // Methodology Header Card
            item {
                StillCard(backgroundColor = StillTokens.elevated) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = StillTokens.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "EMPIRICAL BEHAVIOR ANALYSIS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = StillTokens.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "All insights and recommendations below are calculated strictly from your ${sessions.size} recorded focus sessions. No unsupported assumptions or synthetic conclusions.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = StillTokens.textSecondary,
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            // Recommendations Section
            item {
                SectionHeader(
                    title = "Observed Study Patterns",
                    countLabel = "${insights.size} Insights"
                )
            }

            items(insights) { insight ->
                val typeColor = when (insight.type) {
                    InsightType.STRENGTH -> StillTokens.focus
                    InsightType.OPPORTUNITY -> StillTokens.primary
                    InsightType.ALERT -> StillTokens.distraction
                }

                StillCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = insight.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = StillTokens.textPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = insight.description,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = StillTokens.textSecondary,
                                    lineHeight = 20.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(typeColor.copy(alpha = 0.15f))
                                .border(1.dp, typeColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = insight.metricValue,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = typeColor
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Rationale Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(StillTokens.elevated)
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = StillTokens.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Data Basis:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = StillTokens.textSecondary
                                    )
                                )
                                Text(
                                    text = insight.rationale,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = StillTokens.textSecondary,
                                        fontSize = 11.sp
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

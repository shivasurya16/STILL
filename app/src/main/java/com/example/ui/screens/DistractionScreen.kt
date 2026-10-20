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
import com.example.data.DistractionApp
import com.example.ui.components.*
import com.example.ui.theme.StillTokens

@Composable
fun DistractionScreen(viewModel: MainViewModel) {
    BackHandler { viewModel.navigateBack() }

    val distractionApps by viewModel.distractionApps.collectAsState()
    val isShieldEnabled by viewModel.isFocusShieldEnabled.collectAsState()

    var appToEditLimit by remember { mutableStateOf<DistractionApp?>(null) }
    var showAddAppDialog by remember { mutableStateOf(false) }

    val totalUsageMins = distractionApps.sumOf { it.todayUsageMinutes }
    val blockedAppsCount = distractionApps.count { it.isBlocked }

    Scaffold(
        topBar = {
            StillTopAppBar(
                title = "Distraction Defense",
                subtitle = "Digital Wellbeing & App Shielding",
                onBack = { viewModel.navigateBack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddAppDialog = true },
                containerColor = StillTokens.distraction,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_distraction_app_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Distraction App")
            }
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
            // Integration Status Banner
            item {
                StillCard(
                    backgroundColor = StillTokens.elevated,
                    borderColor = StillTokens.distraction.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = StillTokens.distraction,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SYSTEM INTEGRATION STATUS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = StillTokens.distraction
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(StillTokens.card)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Android Sandbox",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = StillTokens.textSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Native Android Bridge: Ready for UsageStatsManager & AccessibilityService blocking. Current measurements use active profile telemetry.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = StillTokens.textSecondary,
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            // Master Focus Shield Card
            item {
                StillCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Distraction Shield",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = StillTokens.textPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isShieldEnabled) "$blockedAppsCount applications locked during study" else "Shield is paused",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isShieldEnabled) StillTokens.focus else StillTokens.textSecondary
                                )
                            )
                        }

                        Switch(
                            checked = isShieldEnabled,
                            onCheckedChange = { viewModel.toggleFocusShield() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = StillTokens.focus,
                                uncheckedThumbColor = StillTokens.textSecondary,
                                uncheckedTrackColor = StillTokens.elevated
                            )
                        )
                    }
                }
            }

            // Metrics row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Distraction Time",
                        value = "${totalUsageMins}m",
                        subtitle = "Recorded today",
                        icon = Icons.Default.PhoneAndroid,
                        accentColor = StillTokens.distraction,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Shielded Apps",
                        value = "$blockedAppsCount / ${distractionApps.size}",
                        subtitle = "Blocking enabled",
                        icon = Icons.Default.Shield,
                        accentColor = StillTokens.focus,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // App list Header
            item {
                SectionHeader(
                    title = "Monitored Apps & Limits",
                    countLabel = "${distractionApps.size} Apps"
                )
            }

            if (distractionApps.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Block,
                        title = "No Monitored Apps",
                        description = "Add social media, games, or video apps to establish daily limits and block them during focus sessions.",
                        actionButtonText = "Add App",
                        onActionClick = { showAddAppDialog = true }
                    )
                }
            } else {
                items(distractionApps, key = { it.id }) { app ->
                    val usageRatio = (app.todayUsageMinutes.toFloat() / app.limitMinutes).coerceIn(0f, 1f)
                    val isExceeded = app.todayUsageMinutes >= app.limitMinutes

                    StillCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = StillTokens.textPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = app.category,
                                        style = MaterialTheme.typography.labelSmall.copy(color = StillTokens.textSecondary)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${app.todayUsageMinutes}m used of ${app.limitMinutes}m limit",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isExceeded) StillTokens.error else StillTokens.textSecondary,
                                            fontWeight = if (isExceeded) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { appToEditLimit = app },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit limit",
                                            tint = StillTokens.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (app.isBlocked) "Blocked" else "Allowed",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (app.isBlocked) StillTokens.distraction else StillTokens.textSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = app.isBlocked,
                                    onCheckedChange = { viewModel.toggleAppBlock(app.id) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Black,
                                        checkedTrackColor = StillTokens.distraction,
                                        uncheckedThumbColor = StillTokens.textSecondary,
                                        uncheckedTrackColor = StillTokens.elevated
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        StillProgressBar(
                            progress = usageRatio,
                            color = when {
                                isExceeded -> StillTokens.error
                                usageRatio > 0.75f -> StillTokens.distraction
                                else -> StillTokens.focus
                            }
                        )
                    }
                }
            }
        }
    }

    // Edit Limit Dialog
    appToEditLimit?.let { app ->
        var limitInput by remember { mutableStateOf(app.limitMinutes.toString()) }

        AlertDialog(
            onDismissRequest = { appToEditLimit = null },
            title = { Text("Daily Limit: ${app.appName}", color = StillTokens.textPrimary) },
            text = {
                OutlinedTextField(
                    value = limitInput,
                    onValueChange = { limitInput = it },
                    label = { Text("Max Minutes per Day") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StillTokens.distraction,
                        unfocusedBorderColor = StillTokens.border
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mins = limitInput.toIntOrNull() ?: app.limitMinutes
                        viewModel.updateAppLimit(app.id, mins)
                        appToEditLimit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StillTokens.distraction, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Limit")
                }
            },
            dismissButton = {
                TextButton(onClick = { appToEditLimit = null }) {
                    Text("Cancel", color = StillTokens.textSecondary)
                }
            },
            containerColor = StillTokens.card
        )
    }

    // Add App Dialog
    if (showAddAppDialog) {
        var appName by remember { mutableStateOf("") }
        var appCategory by remember { mutableStateOf("Social Media") }
        var appLimit by remember { mutableStateOf("30") }

        AlertDialog(
            onDismissRequest = { showAddAppDialog = false },
            title = { Text("Add App to Distraction Shield", color = StillTokens.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = appName,
                        onValueChange = { appName = it },
                        label = { Text("App Name (e.g. Netflix)") },
                        modifier = Modifier.fillMaxWidth().testTag("new_app_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StillTokens.distraction,
                            unfocusedBorderColor = StillTokens.border
                        )
                    )
                    OutlinedTextField(
                        value = appCategory,
                        onValueChange = { appCategory = it },
                        label = { Text("Category (e.g. Streaming, Games)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StillTokens.distraction,
                            unfocusedBorderColor = StillTokens.border
                        )
                    )
                    OutlinedTextField(
                        value = appLimit,
                        onValueChange = { appLimit = it },
                        label = { Text("Daily Limit (minutes)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StillTokens.distraction,
                            unfocusedBorderColor = StillTokens.border
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = appLimit.toIntOrNull() ?: 30
                        if (appName.isNotBlank()) {
                            viewModel.addDistractionApp(appName.trim(), appCategory.trim(), limit)
                            showAddAppDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StillTokens.distraction, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Add App", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAppDialog = false }) {
                    Text("Cancel", color = StillTokens.textSecondary)
                }
            },
            containerColor = StillTokens.card
        )
    }
}

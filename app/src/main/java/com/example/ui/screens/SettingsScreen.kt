package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.StillTokens

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onOpenImport: () -> Unit
) {
    BackHandler { viewModel.navigateBack() }

    val settings by viewModel.settings.collectAsState()
    val userName by viewModel.userName.collectAsState()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf("") }
    var showResetSampleConfirm by remember { mutableStateOf(false) }
    var showClearAllConfirm by remember { mutableStateOf(false) }
    var showExportToast by remember { mutableStateOf<String?>(null) }

    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            StillTopAppBar(
                title = "Settings",
                subtitle = "Preferences, Canonical JSON & Backups",
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
            // Profile Card with Edit Action
            item {
                StillCard(backgroundColor = StillTokens.elevated) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(StillTokens.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = StillTokens.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                val displayName = userName.ifBlank { settings.studentName.ifBlank { "Personal User" } }
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = StillTokens.textPrimary
                                    )
                                )
                                Text(
                                    text = "Personal Profile · Local Storage",
                                    style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Your profile stays on this device",
                                    style = MaterialTheme.typography.labelSmall.copy(color = StillTokens.focus)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                editNameInput = userName.ifBlank { settings.studentName }
                                showEditNameDialog = true
                            },
                            modifier = Modifier.testTag("settings_edit_name_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile Name",
                                tint = StillTokens.primary
                            )
                        }
                    }
                }
            }

            // Goals & Preferences
            item {
                SectionHeader(title = "Study Targets & Preferences")
                StillCard {
                    // Daily Goal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daily Focus Goal",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = StillTokens.textPrimary
                                )
                            )
                            Text(
                                text = "Target study minutes per day",
                                style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (settings.dailyFocusGoalMinutes > 30) {
                                        viewModel.updateSettings(settings.copy(dailyFocusGoalMinutes = settings.dailyFocusGoalMinutes - 30))
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = StillTokens.textSecondary)
                            }
                            Text(
                                text = "${settings.dailyFocusGoalMinutes / 60}h ${settings.dailyFocusGoalMinutes % 60}m",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = StillTokens.primary
                                )
                            )
                            IconButton(
                                onClick = {
                                    if (settings.dailyFocusGoalMinutes < 600) {
                                        viewModel.updateSettings(settings.copy(dailyFocusGoalMinutes = settings.dailyFocusGoalMinutes + 30))
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = StillTokens.textSecondary)
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = StillTokens.border)

                    // Strict Distraction Shield
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Strict Distraction Blocker",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = StillTokens.textPrimary
                                )
                            )
                            Text(
                                text = "Automatically activate app shield during sessions",
                                style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                            )
                        }

                        Switch(
                            checked = settings.strictDistractionBlock,
                            onCheckedChange = {
                                viewModel.updateSettings(settings.copy(strictDistractionBlock = it))
                            },
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

            // Curriculum & Data Backup Actions
            item {
                SectionHeader(title = "Curriculum & Data Backup")
                StillCard {
                    // Import
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Import Curriculum JSON",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = StillTokens.textPrimary
                                )
                            )
                            Text(
                                text = "Load Academics or Skills JSON document",
                                style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                            )
                        }

                        Button(
                            onClick = onOpenImport,
                            colors = ButtonDefaults.buttonColors(containerColor = StillTokens.primary, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Import")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = StillTokens.border)

                    // Export Academics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Export Academics Curriculum",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = StillTokens.textPrimary
                                )
                            )
                            Text(
                                text = "Canonical v1 JSON for subjects & chapters",
                                style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val json = viewModel.getExportAcademicsJson()
                                clipboardManager.setText(AnnotatedString(json))
                                showExportToast = "Academics Curriculum copied to clipboard!"
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Copy JSON", fontSize = 12.sp)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = StillTokens.border)

                    // Export Skills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Export Skills Curriculum",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = StillTokens.textPrimary
                                )
                            )
                            Text(
                                text = "Canonical v1 JSON for skills & stages",
                                style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val json = viewModel.getExportSkillsJson()
                                clipboardManager.setText(AnnotatedString(json))
                                showExportToast = "Skills Curriculum copied to clipboard!"
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Copy JSON", fontSize = 12.sp)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = StillTokens.border)

                    // Export Full Backup
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Combined Full Backup JSON",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = StillTokens.focus
                                )
                            )
                            Text(
                                text = "Complete package: both curricula, progress & sessions",
                                style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                            )
                        }

                        Button(
                            onClick = {
                                val json = viewModel.getExportFullBackupJson()
                                clipboardManager.setText(AnnotatedString(json))
                                showExportToast = "Complete Backup JSON copied to clipboard!"
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StillTokens.focus,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Export Backup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Reset & Clear Controls
            item {
                SectionHeader(title = "Local Reset Controls")
                StillCard {
                    // Reset to Canonical Demo Data
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Load Canonical Demo Curriculum",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = StillTokens.textPrimary
                                )
                            )
                            Text(
                                text = "Loads Mathematics (Calculus) and Web Development",
                                style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                            )
                        }

                        Button(
                            onClick = { showResetSampleConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = StillTokens.elevated, contentColor = StillTokens.distraction),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Load Demo")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = StillTokens.border)

                    // Clear All User Data
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Clear All Local Data",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = StillTokens.error
                                )
                            )
                            Text(
                                text = "Wipe all courses, skills, history, and preferences",
                                style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                            )
                        }

                        Button(
                            onClick = { showClearAllConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = StillTokens.error, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("clear_all_data_button")
                        ) {
                            Text("Clear All")
                        }
                    }
                }
            }

            // Local Privacy & App Info
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "STILL · Learn. Improve. Grow.",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = StillTokens.textSecondary,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "100% On-Device · No Account or Server Required",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = StillTokens.textSecondary.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }

    // Edit Profile Name Dialog
    if (showEditNameDialog) {
        val trimmed = editNameInput.trim()
        val isNameValid = trimmed.isNotEmpty()
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = {
                Text(
                    text = "Edit Profile Name",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = StillTokens.textPrimary
                    )
                )
            },
            text = {
                Column {
                    Text(
                        text = "Update what STILL calls you on this device. Your curriculum, progress, and study sessions will not be affected.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = StillTokens.textSecondary)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = editNameInput,
                        onValueChange = { editNameInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_edit_name_input"),
                        placeholder = { Text("Enter your name", color = StillTokens.textSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StillTokens.primary,
                            unfocusedBorderColor = StillTokens.border,
                            focusedContainerColor = StillTokens.background,
                            unfocusedContainerColor = StillTokens.background,
                            focusedTextColor = StillTokens.textPrimary,
                            unfocusedTextColor = StillTokens.textPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isNameValid) {
                            viewModel.updateProfileName(trimmed)
                            showEditNameDialog = false
                        }
                    },
                    enabled = isNameValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StillTokens.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("settings_save_name_button")
                ) {
                    Text("Save Name", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel", color = StillTokens.textSecondary)
                }
            },
            containerColor = StillTokens.card
        )
    }

    // Reset Sample Confirmation
    if (showResetSampleConfirm) {
        AlertDialog(
            onDismissRequest = { showResetSampleConfirm = false },
            title = { Text("Load Canonical Demo Curriculum?", color = StillTokens.textPrimary) },
            text = { Text("This will load the canonical Mathematics and Web Development curricula with progress records.", color = StillTokens.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.loadDemoCanonicalData()
                        showResetSampleConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StillTokens.distraction, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Load Canonical Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSampleConfirm = false }) {
                    Text("Cancel", color = StillTokens.textSecondary)
                }
            },
            containerColor = StillTokens.card
        )
    }

    // Clear All Confirmation
    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text("Clear All Local Data?", color = StillTokens.error) },
            text = { Text("Are you absolutely sure? All subjects, skills, progress, and recorded focus history will be permanently deleted.", color = StillTokens.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearAllConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StillTokens.error, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Permanently Wipe")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) {
                    Text("Cancel", color = StillTokens.textSecondary)
                }
            },
            containerColor = StillTokens.card
        )
    }

    showExportToast?.let { toastMsg ->
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { showExportToast = null }) {
                    Text("Dismiss", color = StillTokens.primary)
                }
            },
            containerColor = StillTokens.elevated,
            contentColor = StillTokens.textPrimary
        ) {
            Text(toastMsg)
        }
    }
}

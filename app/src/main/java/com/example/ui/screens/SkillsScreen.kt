package com.example.ui.screens

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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.SampleData
import com.example.data.Skill
import com.example.ui.components.*
import com.example.ui.theme.StillTokens

@Composable
fun SkillsScreen(
    viewModel: MainViewModel,
    onOpenImport: () -> Unit
) {
    val skills by viewModel.skills.collectAsState()
    val skillsProgress by viewModel.skillsProgress.collectAsState()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var showExportToast by remember { mutableStateOf<String?>(null) }

    val overallSummary = viewModel.getOverallSkillsProgress()

    Scaffold(
        topBar = {
            StillTopAppBar(
                title = "Skills",
                subtitle = "${skills.size} Structured Learning Paths · ${overallSummary.percentageInt}% Mastered",
                actions = {
                    IconButton(
                        onClick = onOpenImport,
                        modifier = Modifier.testTag("skills_import_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = "Import Skills JSON",
                            tint = StillTokens.skills
                        )
                    }
                    IconButton(
                        onClick = {
                            val json = viewModel.getExportSkillsJson()
                            clipboardManager.setText(AnnotatedString(json))
                            showExportToast = "Skills Curriculum JSON copied to clipboard!"
                        },
                        modifier = Modifier.testTag("skills_export_curriculum_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Export Skills",
                            tint = StillTokens.textSecondary
                        )
                    }
                    IconButton(
                        onClick = {
                            val json = viewModel.getExportSkillsProgressJson()
                            clipboardManager.setText(AnnotatedString(json))
                            showExportToast = "Skills Progress JSON copied to clipboard!"
                        },
                        modifier = Modifier.testTag("skills_export_progress_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = "Export Progress",
                            tint = StillTokens.focus
                        )
                    }
                }
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
            if (skills.isEmpty()) {
                // Empty state with Canonical Schema
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        StillCard(
                            backgroundColor = StillTokens.card,
                            borderColor = StillTokens.skills.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(StillTokens.skills.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = StillTokens.skills,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "No Skills Curriculum Imported",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = StillTokens.textPrimary
                                    )
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Track deliberate skill progression from structured JSON. Import development roadmaps or load the canonical Web Development template.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = StillTokens.textSecondary,
                                        lineHeight = 20.sp
                                    ),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = onOpenImport,
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = StillTokens.skills,
                                            contentColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Import JSON", fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.validateJson(SampleData.canonicalSkillsJson)
                                            viewModel.applyImport(replaceExisting = false)
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StillTokens.textPrimary)
                                    ) {
                                        Text("Load Web Dev", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Canonical Schema Demonstration Card
                    item {
                        StillCard(backgroundColor = StillTokens.elevated) {
                            Text(
                                text = "CANONICAL SKILLS JSON SPECIFICATION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = StillTokens.skills
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Hierarchy: Skill → Stage → Topic → Optional Subtopic",
                                style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StillTokens.background)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = SampleData.canonicalSkillsJson,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = StillTokens.textSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(skills, key = { it.id }) { skill ->
                        val summary = viewModel.getSkillProgress(skill)

                        // Determine active stage or next incomplete item for "Continue Learning"
                        val activeStage = skill.stages.firstOrNull { stage ->
                            !viewModel.getStageProgress(stage).isComplete
                        } ?: skill.stages.firstOrNull()

                        StillCard(
                            onClick = { viewModel.openSkillDetail(skill.id) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = skill.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = StillTokens.textPrimary
                                        )
                                    )
                                    skill.description?.let { desc ->
                                        if (desc.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = desc,
                                                style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary),
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (summary.isComplete) StillTokens.focus.copy(alpha = 0.15f)
                                            else StillTokens.skills.copy(alpha = 0.15f)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${summary.percentageInt}%",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (summary.isComplete) StillTokens.focus else StillTokens.skills
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${skill.stages.size} Stages · ${summary.completedCount}/${summary.totalCount} Trackable Topics Done",
                                    style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                                )

                                if (activeStage != null) {
                                    Text(
                                        text = "Current: ${activeStage.name}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = StillTokens.skills,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            StillProgressBar(
                                progress = summary.percentage,
                                color = if (summary.isComplete) StillTokens.focus else StillTokens.skills
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Continue Learning Action
                            Button(
                                onClick = { viewModel.openSkillDetail(skill.id) },
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StillTokens.elevated,
                                    contentColor = StillTokens.skills
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StillTokens.skills.copy(alpha = 0.4f))
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Continue Learning", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    showExportToast?.let { toastMsg ->
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { showExportToast = null }) {
                    Text("OK", color = StillTokens.skills)
                }
            },
            containerColor = StillTokens.elevated,
            contentColor = StillTokens.textPrimary
        ) {
            Text(toastMsg)
        }
    }
}

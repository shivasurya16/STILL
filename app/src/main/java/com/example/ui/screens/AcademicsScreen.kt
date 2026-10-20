package com.example.ui.screens

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
import com.example.ui.components.*
import com.example.ui.theme.StillTokens

@Composable
fun AcademicsScreen(
    viewModel: MainViewModel,
    onOpenImport: () -> Unit
) {
    val subjects by viewModel.subjects.collectAsState()
    val academicsProgress by viewModel.academicsProgress.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var showExportToast by remember { mutableStateOf<String?>(null) }

    val filteredSubjects = remember(subjects, searchQuery) {
        if (searchQuery.isBlank()) subjects
        else subjects.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    (it.description?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    val overallSummary = viewModel.getOverallAcademicsProgress()

    Scaffold(
        topBar = {
            StillTopAppBar(
                title = "Academics",
                subtitle = "${subjects.size} Enrolled Courses · ${overallSummary.percentageInt}% Complete",
                actions = {
                    IconButton(
                        onClick = onOpenImport,
                        modifier = Modifier.testTag("academics_import_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = "Import Curriculum JSON",
                            tint = StillTokens.academics
                        )
                    }
                    IconButton(
                        onClick = {
                            val json = viewModel.getExportAcademicsJson()
                            clipboardManager.setText(AnnotatedString(json))
                            showExportToast = "Curriculum JSON copied to clipboard!"
                        },
                        modifier = Modifier.testTag("academics_export_curriculum_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Export Curriculum",
                            tint = StillTokens.textSecondary
                        )
                    }
                    IconButton(
                        onClick = {
                            val json = viewModel.getExportAcademicsProgressJson()
                            clipboardManager.setText(AnnotatedString(json))
                            showExportToast = "Academics Progress JSON copied to clipboard!"
                        },
                        modifier = Modifier.testTag("academics_export_progress_button")
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
            if (subjects.isNotEmpty()) {
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("academics_search_field"),
                    placeholder = {
                        Text("Search courses, chapters, topics...", color = StillTokens.textSecondary)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = StillTokens.textSecondary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = StillTokens.textSecondary)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StillTokens.academics,
                        unfocusedBorderColor = StillTokens.border,
                        focusedContainerColor = StillTokens.card,
                        unfocusedContainerColor = StillTokens.card,
                        focusedTextColor = StillTokens.textPrimary,
                        unfocusedTextColor = StillTokens.textPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            if (subjects.isEmpty()) {
                // Helpful Empty State with Canonical Example
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        StillCard(
                            backgroundColor = StillTokens.card,
                            borderColor = StillTokens.academics.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(StillTokens.academics.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = StillTokens.academics,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "No Academics Curriculum Imported",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = StillTokens.textPrimary
                                    )
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "STILL organizes your curriculum from structured JSON. Import university syllabi or load the canonical template.",
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
                                            containerColor = StillTokens.academics,
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
                                            viewModel.validateJson(SampleData.canonicalAcademicsJson)
                                            viewModel.applyImport(replaceExisting = false)
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StillTokens.textPrimary)
                                    ) {
                                        Text("Load Demo Math", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Canonical Schema Demonstration Card
                    item {
                        StillCard(backgroundColor = StillTokens.elevated) {
                            Text(
                                text = "CANONICAL ACADEMICS JSON SPECIFICATION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = StillTokens.academics
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Hierarchy: Subject → Chapter → Topic → Optional Subtopic",
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
                                    text = SampleData.canonicalAcademicsJson,
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
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredSubjects, key = { it.id }) { subject ->
                        val summary = viewModel.getSubjectProgress(subject)

                        StillCard(
                            onClick = { viewModel.openSubjectDetail(subject.id) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = subject.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = StillTokens.textPrimary
                                        )
                                    )
                                    subject.description?.let { desc ->
                                        if (desc.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = desc,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = StillTokens.textSecondary
                                                ),
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
                                            else StillTokens.academics.copy(alpha = 0.15f)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${summary.percentageInt}%",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (summary.isComplete) StillTokens.focus else StillTokens.academics
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
                                    text = "${subject.chapters.size} Chapters · ${summary.completedCount}/${summary.totalCount} Trackable Items Done",
                                    style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = StillTokens.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            StillProgressBar(
                                progress = summary.percentage,
                                color = if (summary.isComplete) StillTokens.focus else StillTokens.academics
                            )
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
                    Text("OK", color = StillTokens.academics)
                }
            },
            containerColor = StillTokens.elevated,
            contentColor = StillTokens.textPrimary
        ) {
            Text(toastMsg)
        }
    }
}

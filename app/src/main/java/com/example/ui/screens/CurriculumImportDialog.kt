package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.MainViewModel
import com.example.data.SampleData
import com.example.data.ValidationResult
import com.example.ui.components.StillCard
import com.example.ui.theme.StillTokens

@Composable
fun CurriculumImportDialog(
    viewModel: MainViewModel,
    initialJsonText: String? = null,
    onDismiss: () -> Unit
) {
    val validationResult by viewModel.validationResult.collectAsState()
    var pastedJson by remember { mutableStateOf(initialJsonText ?: "") }
    var showReplaceConfirmation by remember { mutableStateOf(false) }

    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    // File picker launcher for .json files
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val text = stream.bufferedReader().use { it.readText() }
                    pastedJson = text
                    viewModel.validateJson(text)
                }
            } catch (e: Exception) {
                // Handled in validation
            }
        }
    }

    Dialog(
        onDismissRequest = {
            viewModel.dismissValidation()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(20.dp))
                .background(StillTokens.card)
                .border(1.dp, StillTokens.border, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (validationResult?.isValid == true) "Preview Curriculum Hierarchy" else "Import Curriculum JSON",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = StillTokens.textPrimary
                            )
                        )
                        Text(
                            text = "Unified JSON parser for Academics & Skills (v1 canonical)",
                            style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.dismissValidation()
                            onDismiss()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = StillTokens.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val result = validationResult

                if (result == null || !result.isValid) {
                    // Input / Paste view
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                filePickerLauncher.launch("application/json")
                            },
                            modifier = Modifier.weight(1f).height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StillTokens.textPrimary)
                        ) {
                            Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload .json", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    pastedJson = clip
                                }
                            },
                            modifier = Modifier.weight(1f).height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StillTokens.textPrimary)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Paste Clipboard", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Templates Quick-Load Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Canonical Templates:",
                            style = MaterialTheme.typography.labelSmall.copy(color = StillTokens.textSecondary)
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                pastedJson = SampleData.canonicalAcademicsJson
                                viewModel.validateJson(pastedJson)
                            },
                            label = { Text("Mathematics", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = StillTokens.elevated,
                                labelColor = StillTokens.academics
                            )
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                pastedJson = SampleData.canonicalSkillsJson
                                viewModel.validateJson(pastedJson)
                            },
                            label = { Text("Web Dev", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = StillTokens.elevated,
                                labelColor = StillTokens.skills
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = pastedJson,
                        onValueChange = {
                            pastedJson = it
                            if (validationResult != null) {
                                viewModel.dismissValidation()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("curriculum_json_input"),
                        placeholder = {
                            Text(
                                text = "{\n  \"version\": 1,\n  \"type\": \"academics\",\n  \"subjects\": [\n    {\n      \"id\": \"math-101\",\n      \"name\": \"Calculus\",\n      \"chapters\": [\n        {\n          \"id\": \"ch-1\",\n          \"name\": \"Limits\",\n          \"topics\": [\n            {\n              \"id\": \"top-1\",\n              \"name\": \"Definition\",\n              \"subtopics\": [{\"id\": \"sub-1\", \"name\": \"Epsilon-Delta\"}]\n            }\n          ]\n        }\n      ]\n    }\n  ]\n}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = StillTokens.textSecondary.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            )
                        },
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = StillTokens.textPrimary,
                            fontSize = 12.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StillTokens.primary,
                            unfocusedBorderColor = StillTokens.border,
                            focusedContainerColor = StillTokens.background,
                            unfocusedContainerColor = StillTokens.background
                        )
                    )

                    // Error presentation if invalid
                    if (result != null && !result.isValid) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(StillTokens.error.copy(alpha = 0.12f))
                                .border(1.dp, StillTokens.error.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = StillTokens.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Validation Failed (${result.errors.size} issues)",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = StillTokens.error,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                result.errors.take(4).forEach { err ->
                                    Text(
                                        text = "• $err",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = StillTokens.textPrimary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                                if (result.errors.size > 4) {
                                    Text(
                                        text = "...and ${result.errors.size - 4} more errors.",
                                        style = MaterialTheme.typography.labelSmall.copy(color = StillTokens.error)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.validateJson(pastedJson)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("validate_import_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StillTokens.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Validate & Preview Hierarchy", fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Valid Preview View
                    val moduleColor = if (result.type == "academics") StillTokens.academics else StillTokens.skills

                    StillCard(backgroundColor = StillTokens.elevated) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(moduleColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = result.type.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = moduleColor
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Ready to Import",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = StillTokens.textPrimary
                                    )
                                )
                                Text(
                                    text = "${result.rootCount} root items · ${result.containerCount} containers · ${result.topicCount} topics · ${result.subtopicCount} subtopics",
                                    style = MaterialTheme.typography.bodySmall.copy(color = StillTokens.textSecondary)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StillTokens.focus.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${result.totalTrackableItems} Trackable",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = StillTokens.focus
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Curriculum Hierarchy Breakdown:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = StillTokens.textSecondary
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(result.summaryHierarchy) { itemText ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StillTokens.background)
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "• $itemText",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = StillTokens.textPrimary)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Notice regarding progress retention
                    Text(
                        text = "Progress for matching IDs will be preserved automatically. Choose 'Update / Merge' to add/update, or 'Replace Existing' to overwrite this module.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = StillTokens.textSecondary,
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.dismissValidation() },
                            modifier = Modifier.weight(0.8f).height(48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Back", color = StillTokens.textSecondary)
                        }

                        Button(
                            onClick = {
                                viewModel.applyImport(replaceExisting = false)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1.3f).height(48.dp).testTag("confirm_merge_import_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = moduleColor,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Update / Merge", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showReplaceConfirmation = true },
                            modifier = Modifier.weight(1.3f).height(48.dp).testTag("confirm_replace_import_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StillTokens.elevated,
                                contentColor = StillTokens.distraction
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StillTokens.distraction.copy(alpha = 0.5f))
                        ) {
                            Text("Replace Existing", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Replace Existing Confirmation Dialog
    if (showReplaceConfirmation) {
        val targetModule = if (validationResult?.type == "academics") "Academics" else "Skills"
        AlertDialog(
            onDismissRequest = { showReplaceConfirmation = false },
            title = { Text("Replace Existing $targetModule?", color = StillTokens.distraction) },
            text = {
                Text(
                    "This action will replace the entire $targetModule curriculum with the imported structure. Progress for any matching IDs will be kept, but unreferenced items will be removed. Unrelated modules are not affected.",
                    color = StillTokens.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReplaceConfirmation = false
                        viewModel.applyImport(replaceExisting = true)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StillTokens.distraction,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirm Replace", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReplaceConfirmation = false }) {
                    Text("Cancel", color = StillTokens.textSecondary)
                }
            },
            containerColor = StillTokens.card
        )
    }
}

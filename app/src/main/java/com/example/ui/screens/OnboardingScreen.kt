package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.theme.StillTokens

data class OnboardingStep(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
fun OnboardingScreen(viewModel: MainViewModel) {
    val steps = remember {
        listOf(
            OnboardingStep(
                title = "Academic Hierarchy",
                subtitle = "Subject → Chapter → Topic",
                description = "Structure every course syllabus with high granularity. Import university JSON syllabi or build your own custom topic checklists with live progress tracking.",
                icon = Icons.Default.MenuBook,
                accentColor = StillTokens.academics
            ),
            OnboardingStep(
                title = "Independent Skills",
                subtitle = "Stage → Milestone → Level Progression",
                description = "Level up practical skills outside the classroom—coding, design, public speaking. Unlock milestones, earn XP, and systematically elevate your abilities.",
                icon = Icons.Default.Psychology,
                accentColor = StillTokens.skills
            ),
            OnboardingStep(
                title = "Focus & Distraction Defense",
                subtitle = "Timestamped Sessions & App Shielding",
                description = "Log precision study sessions that exclude paused time. Monitor digital distractions, uncover productivity patterns, and defend your deep focus.",
                icon = Icons.Default.Shield,
                accentColor = StillTokens.focus
            )
        )
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val step = steps[currentStepIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StillTokens.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        // Top Skip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Step ${currentStepIndex + 1} of ${steps.size}",
                style = MaterialTheme.typography.labelMedium.copy(color = StillTokens.textSecondary)
            )
            TextButton(
                onClick = { viewModel.completeOnboarding() },
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text("Skip", color = StillTokens.textSecondary)
            }
        }

        // Center Animated Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "onboarding_step"
            ) { targetStep ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(targetStep.accentColor.copy(alpha = 0.12f))
                            .border(1.5.dp, targetStep.accentColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = targetStep.icon,
                            contentDescription = null,
                            tint = targetStep.accentColor,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = targetStep.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = StillTokens.textPrimary
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = targetStep.subtitle,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = targetStep.accentColor
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = targetStep.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = StillTokens.textSecondary,
                            lineHeight = 22.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Indicator Dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                steps.indices.forEach { index ->
                    val isSelected = index == currentStepIndex
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (isSelected) 24.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) step.accentColor else StillTokens.elevated)
                    )
                }
            }
        }

        // Bottom Controls
        Button(
            onClick = {
                if (currentStepIndex < steps.size - 1) {
                    currentStepIndex++
                } else {
                    viewModel.completeOnboarding()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .align(Alignment.BottomCenter)
                .testTag("onboarding_next_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = step.accentColor,
                contentColor = if (step.accentColor == StillTokens.focus) Color.Black else Color.White
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = if (currentStepIndex < steps.size - 1) "Continue" else "Get Started With STILL",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

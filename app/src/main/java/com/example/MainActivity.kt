package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.ui.screens.*
import com.example.ui.theme.StillTheme
import com.example.ui.theme.StillTokens

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StillTheme {
                StillApp(viewModel = viewModel)
            }
        }
    }
}

data class NavTabItem(
    val screen: AppScreen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun StillApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val activeElapsed by viewModel.activeSessionElapsedSeconds.collectAsState()
    val userFeedback by viewModel.userFeedbackMessage.collectAsState()

    var showImportDialog by remember { mutableStateOf(false) }

    // Hardware back button handling
    BackHandler(enabled = currentScreen != AppScreen.HOME && currentScreen != AppScreen.SPLASH && currentScreen != AppScreen.NAME_SETUP) {
        viewModel.navigateBack()
    }

    val navTabs = remember {
        listOf(
            NavTabItem(AppScreen.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
            NavTabItem(AppScreen.ACADEMICS, "Academics", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_academics"),
            NavTabItem(AppScreen.SKILLS, "Skills", Icons.Filled.Psychology, Icons.Outlined.Psychology, "nav_skills"),
            NavTabItem(AppScreen.STUDY_SESSION, "Sessions", Icons.Filled.Timer, Icons.Outlined.Timer, "nav_sessions"),
            NavTabItem(AppScreen.MORE, "More", Icons.Filled.GridView, Icons.Outlined.GridView, "nav_more")
        )
    }

    // Determine whether to display the bottom bar
    val showBottomBar = currentScreen in listOf(
        AppScreen.HOME,
        AppScreen.ACADEMICS,
        AppScreen.SKILLS,
        AppScreen.STUDY_SESSION,
        AppScreen.MORE
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = StillTokens.background,
        bottomBar = {
            if (showBottomBar) {
                Column {
                    // Mini Active Session Ticker Bar if running and not currently in the full timer screen
                    if (activeSession != null && currentScreen != AppScreen.STUDY_SESSION) {
                        val s = activeSession!!
                        val mins = activeElapsed / 60
                        val secs = activeElapsed % 60
                        val timeStr = String.format("%02d:%02d", mins, secs)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(StillTokens.elevated)
                                .border(0.5.dp, StillTokens.border)
                                .clickable { viewModel.navigateTo(AppScreen.STUDY_SESSION) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (s.isPaused) StillTokens.distraction else StillTokens.focus)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${s.targetName} (${if (s.isPaused) "Paused" else "Focusing"})",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = StillTokens.textPrimary
                                    ),
                                    maxLines = 1
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = timeStr,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = StillTokens.focus
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = StillTokens.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Navigation Bar respecting WindowInsets navigationBars
                    NavigationBar(
                        containerColor = StillTokens.card,
                        contentColor = StillTokens.textPrimary,
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets.navigationBars,
                        modifier = Modifier.border(1.dp, StillTokens.border)
                    ) {
                        navTabs.forEach { tab ->
                            val isSelected = currentScreen == tab.screen
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.navigateTo(tab.screen) },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        )
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = StillTokens.primary,
                                    selectedTextColor = StillTokens.primary,
                                    indicatorColor = StillTokens.elevated,
                                    unselectedIconColor = StillTokens.textSecondary,
                                    unselectedTextColor = StillTokens.textSecondary
                                ),
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        },
        snackbarHost = {
            userFeedback?.let { msg ->
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(2500)
                    viewModel.clearFeedback()
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Snackbar(
                        containerColor = StillTokens.elevated,
                        contentColor = StillTokens.textPrimary,
                        shape = RoundedCornerShape(12.dp),
                        action = {
                            TextButton(onClick = { viewModel.clearFeedback() }) {
                                Text("OK", color = StillTokens.primary)
                            }
                        }
                    ) {
                        Text(msg)
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentScreen) {
                // 1. Splash
                AppScreen.SPLASH -> SplashScreen(viewModel = viewModel)

                // 2. Onboarding
                AppScreen.ONBOARDING -> OnboardingScreen(viewModel = viewModel)

                // 3. Welcome / Name Setup (First Launch)
                AppScreen.NAME_SETUP -> NameSetupScreen(viewModel = viewModel)

                // 4. Home Dashboard
                AppScreen.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onOpenImport = { showImportDialog = true }
                )

                // 5. Academics
                AppScreen.ACADEMICS -> AcademicsScreen(
                    viewModel = viewModel,
                    onOpenImport = { showImportDialog = true }
                )

                // 6. Subject Detail
                AppScreen.SUBJECT_DETAIL -> SubjectDetailScreen(viewModel = viewModel)

                // 7. Topic Checklist
                AppScreen.TOPIC_CHECKLIST -> TopicChecklistScreen(viewModel = viewModel)

                // 8. Skills
                AppScreen.SKILLS -> SkillsScreen(
                    viewModel = viewModel,
                    onOpenImport = { showImportDialog = true }
                )

                // 9. Skill Progression
                AppScreen.SKILL_PROGRESSION -> SkillProgressionScreen(viewModel = viewModel)

                // 10. Study Session
                AppScreen.STUDY_SESSION -> StudySessionScreen(viewModel = viewModel)

                // 11. Session History
                AppScreen.SESSION_HISTORY -> SessionHistoryScreen(viewModel = viewModel)

                // 12. Distraction Monitoring
                AppScreen.DISTRACTION_MONITORING -> DistractionScreen(viewModel = viewModel)

                // 13. Productivity Intelligence
                AppScreen.PRODUCTIVITY_INTELLIGENCE -> ProductivityIntelligenceScreen(viewModel = viewModel)

                // 14. Focus Tracking
                AppScreen.FOCUS_TRACKING -> FocusTrackingScreen(viewModel = viewModel)

                // 15. Daily Overview
                AppScreen.DAILY_OVERVIEW -> DailyOverviewScreen(viewModel = viewModel)

                // 16. Settings
                AppScreen.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    onOpenImport = { showImportDialog = true }
                )

                // Hub for remaining tools
                AppScreen.MORE -> MoreHubScreen(
                    viewModel = viewModel,
                    onOpenImport = { showImportDialog = true }
                )
            }
        }
    }

    // Global Curriculum Import Dialog
    if (showImportDialog) {
        CurriculumImportDialog(
            viewModel = viewModel,
            onDismiss = { showImportDialog = false }
        )
    }
}

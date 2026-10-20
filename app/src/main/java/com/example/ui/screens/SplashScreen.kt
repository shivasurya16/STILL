package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AppScreen
import com.example.MainViewModel
import com.example.R
import com.example.ui.theme.StillTokens
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(viewModel: MainViewModel) {
    // Check local storage and route after a brief splash display
    LaunchedEffect(Unit) {
        delay(800)
        if (viewModel.hasSavedName()) {
            viewModel.navigateTo(AppScreen.HOME)
        } else {
            viewModel.navigateTo(AppScreen.NAME_SETUP)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StillTokens.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .clickable {
                // Allow tap to proceed immediately
                if (viewModel.hasSavedName()) {
                    viewModel.navigateTo(AppScreen.HOME)
                } else {
                    viewModel.navigateTo(AppScreen.NAME_SETUP)
                }
            }
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Icon Graphic
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(StillTokens.card)
                    .border(1.5.dp, StillTokens.border, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_still_launcher),
                    contentDescription = "STILL Logo",
                    modifier = Modifier.size(96.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "STILL",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = StillTokens.textPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Learn. Improve. Grow.",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    color = StillTokens.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "The structured productivity engine for students.\nCurriculum tracking, deliberate skill progression, and deep focus defense.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = StillTokens.textSecondary,
                    lineHeight = 22.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Action Button at bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Button(
                onClick = {
                    if (viewModel.hasSavedName()) {
                        viewModel.navigateTo(AppScreen.HOME)
                    } else {
                        viewModel.navigateTo(AppScreen.NAME_SETUP)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("splash_get_started_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StillTokens.primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

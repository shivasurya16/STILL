package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.R
import com.example.ui.theme.StillTokens

@Composable
fun NameSetupScreen(viewModel: MainViewModel) {
    var nameInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val trimmedName = remember(nameInput) { nameInput.trim() }
    val isNameValid = trimmedName.isNotEmpty()

    fun submitName() {
        if (isNameValid) {
            focusManager.clearFocus()
            viewModel.saveProfileName(trimmedName)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StillTokens.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(StillTokens.card)
                    .border(1.dp, StillTokens.border, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_still_launcher),
                    contentDescription = "STILL Logo",
                    modifier = Modifier.size(76.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Heading & Subtitle
            Text(
                text = "Welcome to STILL",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = StillTokens.textPrimary
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your personal space to focus, learn, and grow.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = StillTokens.textSecondary,
                    lineHeight = 22.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Input Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(StillTokens.card)
                    .border(1.dp, StillTokens.border, RoundedCornerShape(18.dp))
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "What should we call you?",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = StillTokens.textPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("name_setup_input"),
                        placeholder = {
                            Text(
                                text = "Enter your name",
                                color = StillTokens.textSecondary.copy(alpha = 0.6f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isNameValid) StillTokens.primary else StillTokens.textSecondary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StillTokens.primary,
                            unfocusedBorderColor = StillTokens.border,
                            focusedContainerColor = StillTokens.elevated,
                            unfocusedContainerColor = StillTokens.elevated,
                            focusedTextColor = StillTokens.textPrimary,
                            unfocusedTextColor = StillTokens.textPrimary,
                            cursorColor = StillTokens.primary
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { submitName() }
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            Button(
                onClick = { submitName() },
                enabled = isNameValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("name_setup_submit_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StillTokens.primary,
                    contentColor = Color.White,
                    disabledContainerColor = StillTokens.elevated,
                    disabledContentColor = StillTokens.textSecondary.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Supporting Privacy Text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = StillTokens.focus,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Your profile stays on this device.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = StillTokens.textSecondary,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

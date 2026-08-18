package com.apphider.ui.setup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apphider.ui.theme.HiddenAccent
import com.apphider.ui.theme.HiddenBgEnd
import com.apphider.ui.theme.HiddenBgStart
import com.apphider.ui.theme.RedError
import com.apphider.ui.theme.TextOnDark

/**
 * Elegant initial setup screen for creating the access password.
 * Dark gradient background with minimal, polished layout.
 */
@Composable
fun SetupScreen(
    onSetupComplete: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSetupComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(HiddenBgStart, HiddenBgEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Lock icon
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(HiddenAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("\uD83D\uDD12", fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Title
            Text(
                text = "设置密码",
                style = MaterialTheme.typography.headlineMedium,
                color = TextOnDark,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "请设置 4-6 位数字密码，用于进入隐藏空间",
                style = MaterialTheme.typography.bodyMedium,
                color = TextOnDark.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Password input area with animated step transition
            AnimatedContent(
                targetState = uiState.step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "setup_step"
            ) { step ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val currentPassword = when (step) {
                        SetupStep.ENTER_PASSWORD -> uiState.password
                        SetupStep.CONFIRM_PASSWORD -> uiState.confirmPassword
                    }
                    val hintText = when (step) {
                        SetupStep.ENTER_PASSWORD -> "请输入密码"
                        SetupStep.CONFIRM_PASSWORD -> "请再次输入密码"
                    }

                    Text(
                        text = hintText,
                        color = TextOnDark.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Password dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 6) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            i < currentPassword.length -> HiddenAccent
                                            i == currentPassword.length -> HiddenAccent.copy(alpha = 0.4f)
                                            else -> TextOnDark.copy(alpha = 0.12f)
                                        }
                                    )
                            )
                        }
                    }

                    // Error message
                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = RedError,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Numpad
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "\u232B")
            )

            for (row in keys) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (key in row) {
                        if (key.isEmpty()) {
                            Spacer(modifier = Modifier.size(72.dp))
                        } else {
                            TextButton(
                                onClick = {
                                    when (key) {
                                        "\u232B" -> viewModel.onDeleteClick()
                                        else -> viewModel.onDigitClick(key)
                                    }
                                },
                                modifier = Modifier.size(72.dp),
                                shape = CircleShape,
                            ) {
                                Text(
                                    text = key,
                                    fontSize = if (key == "\u232B") 20.sp else 26.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextOnDark
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Next button
            val currentPassword = when (uiState.step) {
                SetupStep.ENTER_PASSWORD -> uiState.password
                SetupStep.CONFIRM_PASSWORD -> uiState.confirmPassword
            }
            Button(
                onClick = { viewModel.onNextClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentPassword.length >= 4) HiddenAccent else TextOnDark.copy(alpha = 0.08f),
                    contentColor = if (currentPassword.length >= 4) TextOnDark else TextOnDark.copy(alpha = 0.3f)
                ),
                enabled = currentPassword.length >= 4,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (currentPassword.length >= 4) 4.dp else 0.dp
                )
            ) {
                Text(
                    text = if (uiState.step == SetupStep.ENTER_PASSWORD) "下一步" else "完成",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
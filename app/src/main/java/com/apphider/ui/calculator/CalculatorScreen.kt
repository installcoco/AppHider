package com.apphider.ui.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apphider.ui.components.PasswordDialog
import com.apphider.ui.theme.CalculatorBackground
import com.apphider.ui.theme.CalculatorBtnText
import com.apphider.ui.theme.CalculatorDisplayBg
import com.apphider.ui.theme.CalculatorDisplayText
import com.apphider.ui.theme.CalculatorFunctionBtn
import com.apphider.ui.theme.CalculatorNumberBtn
import com.apphider.ui.theme.CalculatorOperatorBtn
import com.apphider.ui.theme.CalculatorOperatorText
import com.apphider.ui.theme.PasswordError
import com.apphider.ui.theme.TextSecondary

/**
 * Calculator disguise screen.
 * Functions as a normal calculator but provides hidden entry to the secret space.
 * Entry methods:
 * 1. Enter password and press =
 * 2. Tap title 5 times to show password dialog
 */
@Composable
fun CalculatorScreen(
    onEnterHiddenSpace: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: CalculatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navigate to hidden space when authenticated
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            viewModel.onNavigatedToHiddenSpace()
            onEnterHiddenSpace()
        }
    }

    // Password dialog
    if (uiState.showPasswordDialog) {
        PasswordDialog(
            title = "输入密码",
            errorMessage = uiState.passwordError,
            onPasswordEntered = { password ->
                // Password is auto-submitted from dialog when 6 digits entered
            },
            onDismiss = { viewModel.onPasswordDialogDismiss() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CalculatorBackground)
    ) {
        // Top bar with settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextSecondary
                )
            }
        }

        // Calculator title (tap 5 times to trigger password entry)
        Text(
            text = "计算器",
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.onTitleTap() }
                .padding(vertical = 8.dp),
            textAlign = TextAlign.Center,
            color = TextSecondary,
            fontSize = 14.sp
        )

        // Display area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CalculatorDisplayBg)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                // Expression (previous operation)
                if (uiState.expression.isNotEmpty()) {
                    Text(
                        text = uiState.expression,
                        color = TextSecondary,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Display value
                Text(
                    text = uiState.displayText,
                    color = CalculatorDisplayText,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Light,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Password mode indicator
        AnimatedVisibility(
            visible = uiState.isPasswordMode,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = "密码模式: ${uiState.passwordBuffer.map { "●" }.joinToString("")}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textAlign = TextAlign.Center,
                color = PasswordError,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Calculator buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: C, %, DEL, ÷
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcButton(text = "C", color = CalculatorFunctionBtn, textColor = CalculatorBtnText, onClick = { viewModel.onClearClick() })
                CalcButton(text = "%", color = CalculatorFunctionBtn, textColor = CalculatorBtnText, onClick = { viewModel.onPercentClick() })
                CalcButton(text = "DEL", color = CalculatorFunctionBtn, textColor = CalculatorBtnText, onClick = { viewModel.onDeleteClick() })
                CalcButton(text = "÷", color = CalculatorOperatorBtn, textColor = CalculatorOperatorText, onClick = { viewModel.onOperatorClick("÷") })
            }
            // Row 2: 7, 8, 9, ×
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcButton(text = "7", color = CalculatorNumberBtn, textColor = CalculatorBtnText, onClick = { viewModel.onDigitClick("7") })
                CalcButton(text = "8", color = CalculatorNumberBtn, textColor = CalculatorBtnText, onClick = { viewModel.onDigitClick("8") })
                CalcButton(text = "9", color = CalculatorNumberBtn, textColor = CalculatorBtnText, onClick = { viewModel.onDigitClick("9") })
                CalcButton(text = "×", color = CalculatorOperatorBtn, textColor = CalculatorOperatorText, onClick = { viewModel.onOperatorClick("×") })
            }
            // Row 3: 4, 5, 6, -
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcButton(text = "4", color = CalculatorNumberBtn, textColor = CalculatorBtnText, onClick = { viewModel.onDigitClick("4") })
                CalcButton(text = "5", color = CalculatorNumberBtn, textColor = CalculatorBtnText, onClick = { viewModel.onDigitClick("5") })
                CalcButton(text = "6", color = CalculatorNumberBtn, textColor = CalculatorBtnText, onClick = { viewModel.onDigitClick("6") })
                CalcButton(text = "-", color = CalculatorOperatorBtn, textColor = CalculatorOperatorText, onClick = { viewModel.onOperatorClick("-") })
            }
            // Row 4: 1, 2, 3, +
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcButton(text = "1", color = CalculatorNumberBtn, textColor = CalculatorBtnText, onClick = { viewModel.onDigitClick("1") })
                CalcButton(text = "2", color = CalculatorNumberBtn, textColor = CalculatorBtnText, onClick = { viewModel.onDigitClick("2") })
                CalcButton(text = "3", color = CalculatorNumberBtn, textColor = CalculatorBtnText, onClick = { viewModel.onDigitClick("3") })
                CalcButton(text = "+", color = CalculatorOperatorBtn, textColor = CalculatorOperatorText, onClick = { viewModel.onOperatorClick("+") })
            }
            // Row 5: 0, ., =
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcButton(
                    text = "0",
                    color = CalculatorNumberBtn,
                    textColor = CalculatorBtnText,
                    modifier = Modifier.weight(2f),
                    onClick = { viewModel.onDigitClick("0") }
                )
                CalcButton(text = ".", color = CalculatorNumberBtn, textColor = CalculatorBtnText, onClick = { viewModel.onDotClick() })
                CalcButton(text = "=", color = CalculatorOperatorBtn, textColor = CalculatorOperatorText, onClick = { viewModel.onEqualsClick() })
            }
        }
    }
}

@Composable
private fun CalcButton(
    text: String,
    color: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(if (text == "0") 2.2f else 1f)
            .height(0dp), // Height determined by aspectRatio
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = textColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = text,
            fontSize = if (text.length > 1) 16.sp else 24.sp,
            fontWeight = FontWeight.Normal,
            color = textColor
        )
    }
}
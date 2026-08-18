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
import com.apphider.ui.theme.*

/**
 * Calculator disguise screen — iOS-calculator-inspired design.
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
            onPasswordEntered = { },
            onDismiss = { viewModel.onPasswordDialogDismiss() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CalcBackground)
    ) {
        // Top bar with settings gear
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, end = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = CalcSecText
                )
            }
        }

        // Title — tap 5 times to trigger password entry
        Text(
            text = "计算器",
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !uiState.isPasswordMode) { viewModel.onTitleTap() }
                .padding(vertical = 4.dp),
            textAlign = TextAlign.Center,
            color = CalcSecText,
            fontSize = 13.sp,
            letterSpacing = 0.5.sp
        )

        // Display area — takes remaining top space
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            // Expression (previous operation)
            if (uiState.expression.isNotEmpty()) {
                Text(
                    text = uiState.expression,
                    color = CalcSecText,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            // Display value
            Text(
                text = uiState.displayText,
                color = CalcDisplayText,
                fontSize = 56.sp,
                fontWeight = FontWeight.Light,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                textAlign = TextAlign.Center,
                color = RedError,
                fontSize = 12.sp
            )
        }

        // Calculator buttons — iOS style spacing
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: C, ±, %, ÷
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CalcBtn(text = "C", color = CalcFuncBtn, textColor = CalcFuncBtnText, onClick = { viewModel.onClearClick() })
                CalcBtn(text = "±", color = CalcFuncBtn, textColor = CalcFuncBtnText, onClick = { viewModel.onNegateClick() })
                CalcBtn(text = "%", color = CalcFuncBtn, textColor = CalcFuncBtnText, onClick = { viewModel.onPercentClick() })
                CalcBtn(text = "÷", color = CalcOpBtn, textColor = CalcOpBtnText, onClick = { viewModel.onOperatorClick("÷") })
            }
            // Row 2: 7, 8, 9, ×
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CalcBtn(text = "7", color = CalcNumBtn, textColor = CalcNumBtnText, onClick = { viewModel.onDigitClick("7") })
                CalcBtn(text = "8", color = CalcNumBtn, textColor = CalcNumBtnText, onClick = { viewModel.onDigitClick("8") })
                CalcBtn(text = "9", color = CalcNumBtn, textColor = CalcNumBtnText, onClick = { viewModel.onDigitClick("9") })
                CalcBtn(text = "×", color = CalcOpBtn, textColor = CalcOpBtnText, onClick = { viewModel.onOperatorClick("×") })
            }
            // Row 3: 4, 5, 6, -
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CalcBtn(text = "4", color = CalcNumBtn, textColor = CalcNumBtnText, onClick = { viewModel.onDigitClick("4") })
                CalcBtn(text = "5", color = CalcNumBtn, textColor = CalcNumBtnText, onClick = { viewModel.onDigitClick("5") })
                CalcBtn(text = "6", color = CalcNumBtn, textColor = CalcNumBtnText, onClick = { viewModel.onDigitClick("6") })
                CalcBtn(text = "-", color = CalcOpBtn, textColor = CalcOpBtnText, onClick = { viewModel.onOperatorClick("-") })
            }
            // Row 4: 1, 2, 3, +
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CalcBtn(text = "1", color = CalcNumBtn, textColor = CalcNumBtnText, onClick = { viewModel.onDigitClick("1") })
                CalcBtn(text = "2", color = CalcNumBtn, textColor = CalcNumBtnText, onClick = { viewModel.onDigitClick("2") })
                CalcBtn(text = "3", color = CalcNumBtn, textColor = CalcNumBtnText, onClick = { viewModel.onDigitClick("3") })
                CalcBtn(text = "+", color = CalcOpBtn, textColor = CalcOpBtnText, onClick = { viewModel.onOperatorClick("+") })
            }
            // Row 5: 0, ., =
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Zero button spans 2 columns
                CalcBtn(
                    text = "0",
                    color = CalcNumBtn,
                    textColor = CalcNumBtnText,
                    modifier = Modifier.weight(2f),
                    onClick = { viewModel.onDigitClick("0") }
                )
                CalcBtn(text = ".", color = CalcNumBtn, textColor = CalcNumBtnText, onClick = { viewModel.onDotClick() })
                CalcBtn(text = "=", color = CalcOpBtn, textColor = CalcOpBtnText, onClick = { viewModel.onEqualsClick() })
            }
        }
    }
}

@Composable
private fun CalcBtn(
    text: String,
    color: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = textColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        )
    ) {
        Text(
            text = text,
            fontSize = if (text.length > 1) 16.sp else 28.sp,
            fontWeight = FontWeight.Normal,
            color = textColor
        )
    }
}
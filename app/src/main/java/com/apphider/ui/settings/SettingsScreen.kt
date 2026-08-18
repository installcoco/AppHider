package com.apphider.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apphider.ui.theme.HiddenAccent
import com.apphider.ui.theme.RedError
import com.apphider.ui.theme.TextOnDark
import com.apphider.ui.theme.TextOnDarkSec

/**
 * Settings screen with clean card-based layout.
 * Includes password change, disguise theme, app management, and about sections.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    // Change Password Dialog
    if (uiState.showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideChangePassword() },
            title = { Text("修改密码", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = uiState.oldPassword,
                        onValueChange = { viewModel.onOldPasswordChange(it) },
                        label = { Text("原密码") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HiddenAccent
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.newPassword,
                        onValueChange = { viewModel.onNewPasswordChange(it) },
                        label = { Text("新密码 (4-6位数字)") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HiddenAccent
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.confirmNewPassword,
                        onValueChange = { viewModel.onConfirmNewPasswordChange(it) },
                        label = { Text("确认新密码") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HiddenAccent
                        )
                    )
                    if (uiState.passwordError != null) {
                        Text(
                            text = uiState.passwordError!!,
                            color = RedError,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.changePassword() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HiddenAccent)
                ) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideChangePassword() }) {
                    Text("取消", color = TextOnDarkSec)
                }
            }
        )
    }

    // Confirm Unhide All Dialog
    if (uiState.showConfirmUnhideAll) {
        AlertDialog(
            onDismissRequest = { viewModel.hideConfirmUnhideAll() },
            title = { Text("确认操作", fontWeight = FontWeight.Bold) },
            text = { Text("确认取消隐藏所有应用？这将恢复所有应用在桌面的图标。") },
            confirmButton = {
                Button(
                    onClick = { viewModel.unhideAllApps() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedError)
                ) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideConfirmUnhideAll() }) {
                    Text("取消", color = TextOnDarkSec)
                }
            }
        )
    }

    // About Dialog
    if (uiState.showAboutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideAbout() },
            title = { Text("关于", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "应用隐藏大师 v1.0.0\n\n" +
                    "本应用仅用于个人隐私保护，请勿用于非法目的。\n\n" +
                    "隐藏机制基于 Android Activity Alias 动态启停技术，\n" +
                    "部分 Android 10+ 厂商 ROM 可能限制该功能。",
                    color = TextOnDarkSec,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.hideAbout() }) { Text("关闭", color = HiddenAccent) }
            }
        )
    }

    // Disclaimer Dialog
    if (uiState.showDisclaimerDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDisclaimer() },
            title = { Text("免责声明", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "本应用仅供个人隐私保护使用。\n\n" +
                    "用户应对使用本应用的行为负全部责任。\n\n" +
                    "开发者不对因使用本应用产生的任何直接或间接损失承担责任。\n\n" +
                    "请遵守当地法律法规。",
                    color = TextOnDarkSec,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.hideDisclaimer() }) { Text("关闭", color = HiddenAccent) }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("设置", color = TextOnDark, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回", tint = TextOnDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Security section
            SectionTitle("安全")
            SettingsCardItem(
                icon = Icons.Default.Key,
                title = "修改密码",
                subtitle = "更改进入隐藏空间的访问密码",
                onClick = { viewModel.showChangePassword() }
            )
            SettingsCardItem(
                icon = Icons.Default.Lock,
                title = "指纹解锁",
                subtitle = "使用指纹快速进入隐藏空间",
                trailing = {
                    Switch(
                        checked = uiState.biometricEnabled,
                        onCheckedChange = { viewModel.onBiometricToggle(it) },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = HiddenAccent,
                            checkedThumbColor = Color.White
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Disguise section
            SectionTitle("伪装")
            SettingsCardItem(
                icon = Icons.Default.Palette,
                title = "伪装主题",
                subtitle = when (uiState.currentTheme) {
                    "calculator" -> "计算器"
                    "notes" -> "记事本"
                    "weather" -> "天气"
                    else -> "计算器"
                },
                onClick = {
                    val next = when (uiState.currentTheme) {
                        "calculator" -> "notes"
                        "notes" -> "weather"
                        else -> "calculator"
                    }
                    viewModel.onThemeChange(next)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // App management section
            SectionTitle("应用管理")
            SettingsCardItem(
                icon = Icons.Default.VisibilityOff,
                title = "管理隐藏应用",
                subtitle = "查看和取消隐藏应用",
                onClick = { viewModel.showConfirmUnhideAll() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // About section
            SectionTitle("关于")
            SettingsCardItem(
                icon = Icons.Default.Info,
                title = "关于",
                subtitle = "版本信息与功能说明",
                onClick = { viewModel.showAbout() }
            )
            SettingsCardItem(
                icon = Icons.Default.Warning,
                title = "免责声明",
                subtitle = "使用条款与免责说明",
                onClick = { viewModel.showDisclaimer() }
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = HiddenAccent,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp, top = 4.dp)
    )
}

@Composable
private fun SettingsCardItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = HiddenAccent,
                modifier = Modifier.padding(end = 14.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextOnDark,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnDarkSec
                )
            }
            if (trailing != null) {
                trailing()
            } else {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "更多",
                    tint = TextOnDarkSec.copy(alpha = 0.4f)
                )
            }
        }
    }
}
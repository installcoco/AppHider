package com.apphider.ui.admin

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apphider.service.AppHiderDeviceAdminReceiver
import com.apphider.ui.theme.HiddenAccent
import com.apphider.ui.theme.HiddenBgEnd
import com.apphider.ui.theme.HiddenBgStart
import com.apphider.ui.theme.TextOnDark

/**
 * Screen that guides the user to activate device admin permission.
 * Required for DevicePolicyManager.setApplicationHidden() to work.
 */
@Composable
fun AdminActivationScreen(
    onActivated: () -> Unit
) {
    val context = LocalContext.current
    var isActive by remember { mutableStateOf(checkAdminActive(context)) }

    // Poll for admin activation
    LaunchedEffect(Unit) {
        while (!isActive) {
            kotlinx.coroutines.delay(500)
            isActive = checkAdminActive(context)
        }
        onActivated()
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
            // Shield icon
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(HiddenAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("\uD83D\uDEE1\uFE0F", fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "激活设备管理器",
                style = MaterialTheme.typography.headlineMedium,
                color = TextOnDark,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "AppHider 需要设备管理器权限才能隐藏应用。\n" +
                       "点击下方按钮后，在系统设置中点击「激活」。\n\n" +
                       "我们仅使用隐藏应用功能，不会锁定或擦除您的设备。",
                style = MaterialTheme.typography.bodyMedium,
                color = TextOnDark.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                        putExtra(
                            DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                            ComponentName(context, AppHiderDeviceAdminReceiver::class.java)
                        )
                        putExtra(
                            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                            "激活后可以隐藏应用图标，保护隐私"
                        )
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HiddenAccent,
                    contentColor = TextOnDark
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "前往激活",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "激活后自动进入...",
                color = TextOnDark.copy(alpha = 0.3f),
                fontSize = 12.sp
            )
        }
    }
}

private fun checkAdminActive(context: Context): Boolean {
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        ?: return false
    val component = ComponentName(context, AppHiderDeviceAdminReceiver::class.java)
    return dpm.isAdminActive(component)
}
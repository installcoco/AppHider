package com.apphider.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AppDarkColorScheme = darkColorScheme(
    primary = HiddenAccent,
    secondary = HiddenAccentSoft,
    tertiary = GreenSuccess,
    background = CalcBackground,
    surface = SettingsCard,
    surfaceVariant = HiddenCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextOnDark,
    onSurface = TextOnDark,
    onSurfaceVariant = TextOnDarkSec,
    error = RedError,
    onError = Color.White,
    outline = HiddenCardBorder
)

private val AppLightColorScheme = lightColorScheme(
    primary = HiddenAccent,
    secondary = HiddenAccentSoft,
    tertiary = GreenSuccess,
    background = Color(0xFFF2F2F7),
    surface = Color.White,
    surfaceVariant = Color(0xFFE5E5EA),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1C1C1E),
    onSurface = Color(0xFF1C1C1E),
    onSurfaceVariant = Color(0xFF8E8E93),
    error = RedError,
    onError = Color.White,
    outline = Color(0x1A000000)
)

@Composable
fun AppHiderTheme(
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) AppDarkColorScheme else AppLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
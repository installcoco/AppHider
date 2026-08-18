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
    secondary = HiddenAccentLight,
    tertiary = GreenSuccess,
    background = HiddenBgStart,
    surface = HiddenSurface,
    surfaceVariant = HiddenCardBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextOnDark,
    onSurface = TextOnDark,
    onSurfaceVariant = TextOnDark.copy(alpha = 0.7f),
    error = RedError,
    onError = Color.White,
    outline = HiddenCardBorder
)

private val AppLightColorScheme = lightColorScheme(
    primary = HiddenAccent,
    secondary = HiddenAccentLight,
    tertiary = GreenSuccess,
    background = LightSurface,
    surface = LightSurfaceVariant,
    surfaceVariant = CalcBackground,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = RedError,
    onError = Color.White,
    outline = LightDivider
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
        typography = AppHiderTypography,
        content = content
    )
}
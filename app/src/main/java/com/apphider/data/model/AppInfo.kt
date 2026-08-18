package com.apphider.data.model

import android.graphics.drawable.Drawable

/**
 * Data class representing an installed application on the device.
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val isSystemApp: Boolean = false,
    val isHidden: Boolean = false,
    val isSelected: Boolean = false
)
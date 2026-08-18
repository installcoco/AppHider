package com.apphider.domain.model

/**
 * Domain model representing an installed application on the device.
 * This is a data-layer independent representation used by the UI layer.
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean = false,
    val isHidden: Boolean = false,
    val isSelected: Boolean = false
)
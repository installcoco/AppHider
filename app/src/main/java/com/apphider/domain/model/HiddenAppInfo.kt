package com.apphider.domain.model

/**
 * Domain model representing a hidden application.
 */
data class HiddenAppInfo(
    val packageName: String,
    val appName: String,
    val aliasSlotIndex: Int,
    val hiddenAtTimestamp: Long = System.currentTimeMillis()
)
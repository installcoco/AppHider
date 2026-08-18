package com.apphider.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing hidden application records.
 */
@Entity(tableName = "hidden_apps")
data class HiddenAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val aliasSlotIndex: Int,
    val hiddenAtTimestamp: Long = System.currentTimeMillis()
)
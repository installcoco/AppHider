package com.apphider.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for the application.
 * Stores the list of hidden applications.
 */
@Database(
    entities = [HiddenAppEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun hiddenAppDao(): HiddenAppDao

    companion object {
        const val DATABASE_NAME = "apphider_db"
    }
}
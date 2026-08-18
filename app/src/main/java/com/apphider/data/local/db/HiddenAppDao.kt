package com.apphider.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for hidden applications.
 */
@Dao
interface HiddenAppDao {

    @Query("SELECT * FROM hidden_apps ORDER BY appName ASC")
    fun getAllHiddenApps(): Flow<List<HiddenAppEntity>>

    @Query("SELECT * FROM hidden_apps ORDER BY appName ASC")
    suspend fun getAllHiddenAppsOnce(): List<HiddenAppEntity>

    @Query("SELECT * FROM hidden_apps WHERE packageName = :packageName")
    suspend fun getHiddenApp(packageName: String): HiddenAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(hiddenApp: HiddenAppEntity)

    @Delete
    suspend fun delete(hiddenApp: HiddenAppEntity)

    @Query("DELETE FROM hidden_apps WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)

    @Query("DELETE FROM hidden_apps")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM hidden_apps")
    suspend fun getCount(): Int

    @Query("SELECT aliasSlotIndex FROM hidden_apps WHERE packageName = :packageName")
    suspend fun getAliasSlotForPackage(packageName: String): Int?

    @Query("SELECT packageName FROM hidden_apps WHERE aliasSlotIndex = :slotIndex")
    suspend fun getPackageNameForSlot(slotIndex: Int): String?
}
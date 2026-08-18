package com.apphider.domain.repository

import android.graphics.drawable.Drawable
import com.apphider.domain.model.HiddenAppInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing installed applications and hiding/unhiding operations.
 */
interface AppRepository {

    /**
     * Scans all installed third-party launcher apps on the device.
     * Excludes system apps and the app itself.
     */
    suspend fun getInstalledThirdPartyApps(): List<com.apphider.domain.model.AppInfo>

    /**
     * Returns a Flow of all currently hidden applications.
     */
    fun getHiddenApps(): Flow<List<HiddenAppInfo>>

    /**
     * Returns a one-shot list of all currently hidden applications.
     */
    suspend fun getHiddenAppsOnce(): List<HiddenAppInfo>

    /**
     * Hides the specified application by assigning it to an available alias slot
     * and disabling the alias to remove it from the launcher.
     */
    suspend fun hideApp(packageName: String, appName: String): Result<Unit>

    /**
     * Unhides the specified application by re-enabling its alias slot.
     */
    suspend fun unhideApp(packageName: String): Result<Unit>

    /**
     * Unhides all hidden applications.
     */
    suspend fun unhideAllApps(): Result<Unit>

    /**
     * Retrieves the icon drawable for a hidden app (for display in hidden space).
     */
    suspend fun getHiddenAppIcon(packageName: String): Drawable?

    /**
     * Launches the specified application by its package name.
     */
    suspend fun launchApp(packageName: String): Result<Unit>

    /**
     * Checks if the specified application is still installed.
     */
    suspend fun isAppInstalled(packageName: String): Boolean

    /**
     * Returns the total number of available alias slots.
     */
    fun getAvailableSlotCount(): Int
}
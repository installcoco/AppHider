package com.apphider.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.util.Log
import com.apphider.data.local.db.HiddenAppDao
import com.apphider.data.local.db.HiddenAppEntity
import com.apphider.domain.model.AppInfo as DomainAppInfo
import com.apphider.domain.repository.AppRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [AppRepository] that manages installed app discovery
 * and hiding/unhiding via Activity Alias mechanism.
 */
@Singleton
class AppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val hiddenAppDao: HiddenAppDao
) : AppRepository {

    companion object {
        private const val TAG = "AppRepository"
        private const val ALIAS_PREFIX = "com.apphider.alias.AliasSlot"
        private const val TOTAL_ALIAS_SLOTS = 20
        private const val LAUNCHER_CATEGORY = "android.intent.category.LAUNCHER"
        private const val MAIN_ACTION = "android.intent.action.MAIN"
    }

    override suspend fun getInstalledThirdPartyApps(): List<DomainAppInfo> = withContext(Dispatchers.IO) {
        val hiddenPackages = hiddenAppDao.getAllHiddenAppsOnce().map { it.packageName }.toSet()

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = context.packageManager.queryIntentActivities(
            intent, 0
        )

        resolveInfos
            .filter { resolveInfo ->
                val packageName = resolveInfo.activityInfo?.packageName ?: return@filter false
                // Exclude system apps and our own app
                val isSystemApp = (resolveInfo.activityInfo?.applicationInfo?.flags
                    ?: 0) and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0
                val isOurApp = packageName == context.packageName
                !isSystemApp && !isOurApp
            }
            .distinctBy { it.activityInfo.packageName }
            .map { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                val appName = resolveInfo.loadLabel(context.packageManager).toString()
                DomainAppInfo(
                    packageName = packageName,
                    appName = appName,
                    isSystemApp = false,
                    isHidden = hiddenPackages.contains(packageName)
                )
            }
            .sortedBy { it.appName.lowercase() }
    }

    override fun getHiddenApps(): Flow<List<com.apphider.domain.model.HiddenAppInfo>> {
        return hiddenAppDao.getAllHiddenApps().map { entities ->
            entities.map { entity ->
                com.apphider.domain.model.HiddenAppInfo(
                    packageName = entity.packageName,
                    appName = entity.appName,
                    aliasSlotIndex = entity.aliasSlotIndex,
                    hiddenAtTimestamp = entity.hiddenAtTimestamp
                )
            }
        }
    }

    override suspend fun getHiddenAppsOnce(): List<com.apphider.domain.model.HiddenAppInfo> {
        return hiddenAppDao.getAllHiddenAppsOnce().map { entity ->
            com.apphider.domain.model.HiddenAppInfo(
                packageName = entity.packageName,
                appName = entity.appName,
                aliasSlotIndex = entity.aliasSlotIndex,
                hiddenAtTimestamp = entity.hiddenAtTimestamp
            )
        }
    }

    override suspend fun hideApp(packageName: String, appName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Find an available alias slot
            val usedSlots = hiddenAppDao.getAllHiddenAppsOnce().map { it.aliasSlotIndex }.toSet()
            var availableSlot = -1
            for (i in 1..TOTAL_ALIAS_SLOTS) {
                if (i !in usedSlots) {
                    availableSlot = i
                    break
                }
            }
            if (availableSlot == -1) {
                return@withContext Result.failure(Exception("No available alias slots (max $TOTAL_ALIAS_SLOTS)"))
            }

            // Disable the alias slot to remove it from launcher
            val aliasName = "$ALIAS_PREFIX${String.format("%02d", availableSlot)}"
            context.packageManager.setComponentEnabledSetting(
                android.content.ComponentName(context, aliasName),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )

            // Save to database
            hiddenAppDao.insert(
                HiddenAppEntity(
                    packageName = packageName,
                    appName = appName,
                    aliasSlotIndex = availableSlot
                )
            )

            Log.d(TAG, "App hidden: $packageName -> alias slot $availableSlot")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to hide app: $packageName", e)
            Result.failure(e)
        }
    }

    override suspend fun unhideApp(packageName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val slotIndex = hiddenAppDao.getAliasSlotForPackage(packageName) ?: return@withContext Result.failure(
                Exception("App not found in hidden list: $packageName")
            )

            // Re-enable the alias slot
            val aliasName = "$ALIAS_PREFIX${String.format("%02d", slotIndex)}"
            context.packageManager.setComponentEnabledSetting(
                android.content.ComponentName(context, aliasName),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            // Remove from database
            hiddenAppDao.deleteByPackageName(packageName)

            Log.d(TAG, "App unhidden: $packageName (slot $slotIndex)")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unhide app: $packageName", e)
            Result.failure(e)
        }
    }

    override suspend fun unhideAllApps(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val allHidden = hiddenAppDao.getAllHiddenAppsOnce()
            for (entity in allHidden) {
                val aliasName = "$ALIAS_PREFIX${String.format("%02d", entity.aliasSlotIndex)}"
                context.packageManager.setComponentEnabledSetting(
                    android.content.ComponentName(context, aliasName),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
            hiddenAppDao.deleteAll()
            Log.d(TAG, "All apps unhidden (${allHidden.size})")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unhide all apps", e)
            Result.failure(e)
        }
    }

    override suspend fun getHiddenAppIcon(packageName: String): Drawable? = withContext(Dispatchers.IO) {
        try {
            val packageManager = context.packageManager
            val info = packageManager.getApplicationInfo(packageName, 0)
            info.loadIcon(packageManager)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun launchApp(packageName: String): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Result.success(Unit)
            } else {
                Result.failure(Exception("No launch intent found for $packageName"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch app: $packageName", e)
            Result.failure(e)
        }
    }

    override suspend fun isAppInstalled(packageName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            context.packageManager.getApplicationInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun getAvailableSlotCount(): Int {
        return TOTAL_ALIAS_SLOTS
    }
}
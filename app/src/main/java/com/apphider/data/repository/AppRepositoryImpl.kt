package com.apphider.data.repository

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
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
import com.apphider.service.AppHiderDeviceAdminReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [AppRepository] using DevicePolicyManager.setApplicationHidden().
 * This is the standard Android API for truly hiding apps from the launcher.
 * Requires the user to activate device admin for this app.
 */
@Singleton
class AppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val hiddenAppDao: HiddenAppDao
) : AppRepository {

    companion object {
        private const val TAG = "AppRepository"
    }

    private val devicePolicyManager: DevicePolicyManager?
        get() = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager

    private val adminComponent: ComponentName
        get() = ComponentName(context, AppHiderDeviceAdminReceiver::class.java)

    override fun isDeviceAdminActive(): Boolean {
        return devicePolicyManager?.isAdminActive(adminComponent) == true
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
            // Check device admin
            val dpm = devicePolicyManager
            if (dpm == null || !dpm.isAdminActive(adminComponent)) {
                return@withContext Result.failure(Exception("需要先激活设备管理器"))
            }

            // Hide the app using DevicePolicyManager
            val success = dpm.setApplicationHidden(adminComponent, packageName, true)
            if (!success) {
                return@withContext Result.failure(Exception("隐藏失败，此应用可能不支持被隐藏"))
            }

            // Save to database
            hiddenAppDao.insert(
                HiddenAppEntity(
                    packageName = packageName,
                    appName = appName,
                    aliasSlotIndex = 0,
                    hiddenAtTimestamp = System.currentTimeMillis()
                )
            )

            Log.d(TAG, "App hidden via DPM: $packageName")
            Result.success(Unit)
        } catch (e: SecurityException) {
            Log.e(TAG, "Device admin not active: $packageName", e)
            Result.failure(Exception("设备管理器未激活，请在设置中激活"))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to hide app: $packageName", e)
            Result.failure(e)
        }
    }

    override suspend fun unhideApp(packageName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dpm = devicePolicyManager
            if (dpm == null || !dpm.isAdminActive(adminComponent)) {
                return@withContext Result.failure(Exception("设备管理器未激活"))
            }

            // Unhide the app
            dpm.setApplicationHidden(adminComponent, packageName, false)

            // Remove from database
            hiddenAppDao.deleteByPackageName(packageName)

            Log.d(TAG, "App unhidden: $packageName")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unhide app: $packageName", e)
            Result.failure(e)
        }
    }

    override suspend fun unhideAllApps(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dpm = devicePolicyManager
            if (dpm == null || !dpm.isAdminActive(adminComponent)) {
                return@withContext Result.failure(Exception("设备管理器未激活"))
            }

            val allHidden = hiddenAppDao.getAllHiddenAppsOnce()
            for (entity in allHidden) {
                dpm.setApplicationHidden(adminComponent, entity.packageName, false)
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
                Result.failure(Exception("无法启动该应用"))
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
}
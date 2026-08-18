package com.apphider.service

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.apphider.data.local.db.HiddenAppDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Boot receiver that re-applies hidden app states after device reboot.
 * DevicePolicyManager's setApplicationHidden state persists across reboots
 * automatically, but this receiver ensures database consistency.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    @Inject
    lateinit var hiddenAppDao: HiddenAppDao

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i(TAG, "Boot completed - verifying hidden app states")
            scope.launch {
                try {
                    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
                    val adminComponent = ComponentName(context, AppHiderDeviceAdminReceiver::class.java)

                    if (dpm == null || !dpm.isAdminActive(adminComponent)) {
                        Log.w(TAG, "Device admin not active, skipping verification")
                        return@launch
                    }

                    val hiddenApps = hiddenAppDao.getAllHiddenAppsOnce()
                    var verifiedCount = 0

                    for (app in hiddenApps) {
                        // Verify the app is still hidden
                        if (!dpm.isApplicationHidden(adminComponent, app.packageName)) {
                            Log.w(TAG, "Re-hiding ${app.packageName}")
                            dpm.setApplicationHidden(adminComponent, app.packageName, true)
                            verifiedCount++
                        }
                    }
                    Log.i(TAG, "Verified ${hiddenApps.size} hidden apps ($verifiedCount re-applied)")
                } catch (e: Exception) {
                    Log.e(TAG, "Error verifying hidden app states", e)
                }
            }
        }
    }
}
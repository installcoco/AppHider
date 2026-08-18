package com.apphider.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
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
 * Android's PackageManager state persists across reboots, but this ensures
 * consistency between our database and the actual PackageManager state.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AppHiderService"
        private const val ALIAS_PREFIX = "com.apphider.alias.AliasSlot"
    }

    @Inject
    lateinit var hiddenAppDao: HiddenAppDao

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i(TAG, "Boot completed - verifying hidden app states")
            scope.launch {
                try {
                    val hiddenApps = hiddenAppDao.getAllHiddenAppsOnce()
                    for (app in hiddenApps) {
                        val aliasName = "$ALIAS_PREFIX${String.format("%02d", app.aliasSlotIndex)}"
                        // Verify the alias is still disabled
                        val state = context.packageManager.getComponentEnabledSetting(
                            android.content.ComponentName(context, aliasName)
                        )
                        if (state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                            Log.w(TAG, "Re-disabling alias for ${app.packageName}")
                            context.packageManager.setComponentEnabledSetting(
                                android.content.ComponentName(context, aliasName),
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                PackageManager.DONT_KILL_APP
                            )
                        }
                    }
                    Log.i(TAG, "Verified ${hiddenApps.size} hidden app states")
                } catch (e: Exception) {
                    Log.e(TAG, "Error verifying hidden app states", e)
                }
            }
        }
    }
}
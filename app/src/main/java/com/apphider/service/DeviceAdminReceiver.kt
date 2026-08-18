package com.apphider.service

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Device Admin Receiver for AppHider.
 * Required for DevicePolicyManager.setApplicationHidden() to work.
 * This is the standard Android API for hiding/unhiding apps from the launcher.
 */
class AppHiderDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        Log.i(TAG, "Device admin enabled")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        Log.w(TAG, "Device admin disabled - hidden apps will become visible")
    }

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        Log.i(TAG, "Profile provisioning complete")
    }

    companion object {
        private const val TAG = "AppHiderAdmin"
    }
}
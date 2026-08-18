package com.apphider

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

/**
 * Transparent launcher activity that serves as the target for activity-alias slots.
 * When an alias is enabled, tapping its icon in the launcher would trigger this activity.
 * Since aliases are disabled when apps are hidden, this activity should never actually launch.
 * It exists purely as a manifest requirement for the activity-alias mechanism.
 *
 * Security note: This activity is transparent and finishes immediately to avoid
 * any visual artifacts if somehow triggered.
 */
class LauncherActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LauncherActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This activity should never be visible since its aliases are disabled
        Log.w(TAG, "LauncherActivity triggered unexpectedly - this should not happen")
        finish()
    }
}
package com.apphider

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity

/**
 * Dialer entry activity that responds to the secret dialer code.
 * Triggered by the custom URL scheme "apphider://" or via the dialer code.
 *
 * When the user dials the secret code (*#*#1234#*#*), this activity
 * receives the intent and launches the main calculator/disguise activity.
 *
 * Note: Due to Android restrictions on dialer codes, this activity uses
 * a custom URL scheme approach. The actual dialer code detection requires
 * a broadcast receiver for the secret code, which is registered in the manifest.
 */
class DialerEntryActivity : ComponentActivity() {

    companion object {
        private const val TAG = "DialerEntryActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "DialerEntryActivity triggered")

        try {
            val launchIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("from_dialer", true)
            }
            startActivity(launchIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch MainActivity", e)
        }

        finish()
    }
}
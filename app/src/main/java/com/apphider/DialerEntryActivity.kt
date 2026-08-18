package com.apphider

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.apphider.data.local.datastore.SecurityPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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
@AndroidEntryPoint
class DialerEntryActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DialerEntryActivity"
        private const val SECRET_CODE = "*#*#1234#*#*"
    }

    @Inject
    lateinit var securityPreferences: SecurityPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "DialerEntryActivity triggered")

        // Check if this was triggered by the secret code or custom scheme
        val action = intent?.action
        val data = intent?.dataString

        try {
            // Launch the main activity (calculator disguise)
            val launchIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("from_dialer", true)
            }
            startActivity(launchIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch MainActivity", e)
        }

        // Finish immediately - this activity is transparent
        finish()
    }
}
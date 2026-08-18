package com.apphider

import android.util.Log
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for AppHider.
 * Initializes Hilt dependency injection.
 * No network calls, no data collection - privacy first.
 */
@HiltAndroidApp
class AppHiderApp : android.app.Application() {

    companion object {
        private const val TAG = "AppHider"
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "AppHider initialized. Version: 1.0.0")
        Log.i(TAG, "This app is for personal privacy protection only.")
    }
}
package com.apphider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.apphider.data.local.datastore.SecurityPreferences
import com.apphider.ui.navigation.AppNavGraph
import com.apphider.ui.navigation.Routes
import com.apphider.ui.theme.AppHiderTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Single Activity entry point for the AppHider application.
 * Determines the start destination based on whether the user has completed setup.
 * The app is disguised as a "Calculator" app on the launcher.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var securityPreferences: SecurityPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppHiderTheme(isDarkTheme = false) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    var startDestination by remember { mutableStateOf<String?>(null) }

                    // Determine if setup is needed
                    LaunchedEffect(Unit) {
                        val isSetupComplete = securityPreferences.isSetupCompleteFlow.first()
                        startDestination = if (isSetupComplete) {
                            Routes.CALCULATOR
                        } else {
                            Routes.SETUP
                        }
                    }

                    startDestination?.let { destination ->
                        AppNavGraph(
                            navController = navController,
                            startDestination = destination
                        )
                    }
                }
            }
        }
    }
}
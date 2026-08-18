package com.apphider.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.securityDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "security_preferences"
)

/**
 * DataStore-based preferences for security-related settings.
 * Stores encrypted password hash, lockout state, and disguise theme.
 */
@Singleton
class SecurityPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_ENCRYPTED_PASSWORD = stringPreferencesKey("encrypted_password")
        private val KEY_PASSWORD_IV = stringPreferencesKey("password_iv")
        private val KEY_FAILED_ATTEMPTS = intPreferencesKey("failed_attempts")
        private val KEY_LOCKOUT_UNTIL = longPreferencesKey("lockout_until")
        private val KEY_IS_SETUP_COMPLETE = booleanPreferencesKey("is_setup_complete")
        private val KEY_DISGUISE_THEME = stringPreferencesKey("disguise_theme")
        private val KEY_SECURITY_QUESTION = stringPreferencesKey("security_question")
        private val KEY_SECURITY_ANSWER_HASH = stringPreferencesKey("security_answer_hash")
        private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")

        const val MAX_FAILED_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MS = 30_000L // 30 seconds
    }

    /** Encrypted password (Base64 encoded) */
    val encryptedPasswordFlow: Flow<String?> = context.securityDataStore.data.map { prefs ->
        prefs[KEY_ENCRYPTED_PASSWORD]
    }

    /** Password initialization vector (Base64 encoded) */
    val passwordIvFlow: Flow<String?> = context.securityDataStore.data.map { prefs ->
        prefs[KEY_PASSWORD_IV]
    }

    /** Number of consecutive failed password attempts */
    val failedAttemptsFlow: Flow<Int> = context.securityDataStore.data.map { prefs ->
        prefs[KEY_FAILED_ATTEMPTS] ?: 0
    }

    /** Timestamp until which the app is locked */
    val lockoutUntilFlow: Flow<Long> = context.securityDataStore.data.map { prefs ->
        prefs[KEY_LOCKOUT_UNTIL] ?: 0L
    }

    /** Whether initial setup (password creation) is complete */
    val isSetupCompleteFlow: Flow<Boolean> = context.securityDataStore.data.map { prefs ->
        prefs[KEY_IS_SETUP_COMPLETE] ?: false
    }

    /** Current disguise theme (calculator, notes, weather) */
    val disguiseThemeFlow: Flow<String> = context.securityDataStore.data.map { prefs ->
        prefs[KEY_DISGUISE_THEME] ?: "calculator"
    }

    /** Whether biometric authentication is enabled */
    val biometricEnabledFlow: Flow<Boolean> = context.securityDataStore.data.map { prefs ->
        prefs[KEY_BIOMETRIC_ENABLED] ?: false
    }

    suspend fun setEncryptedPassword(password: String, iv: String) {
        context.securityDataStore.edit { prefs ->
            prefs[KEY_ENCRYPTED_PASSWORD] = password
            prefs[KEY_PASSWORD_IV] = iv
        }
    }

    suspend fun setSetupComplete(isComplete: Boolean) {
        context.securityDataStore.edit { prefs ->
            prefs[KEY_IS_SETUP_COMPLETE] = isComplete
        }
    }

    suspend fun setFailedAttempts(attempts: Int) {
        context.securityDataStore.edit { prefs ->
            prefs[KEY_FAILED_ATTEMPTS] = attempts
        }
    }

    suspend fun setLockoutUntil(timestamp: Long) {
        context.securityDataStore.edit { prefs ->
            prefs[KEY_LOCKOUT_UNTIL] = timestamp
        }
    }

    suspend fun setDisguiseTheme(theme: String) {
        context.securityDataStore.edit { prefs ->
            prefs[KEY_DISGUISE_THEME] = theme
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.securityDataStore.edit { prefs ->
            prefs[KEY_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun resetFailedAttempts() {
        context.securityDataStore.edit { prefs ->
            prefs[KEY_FAILED_ATTEMPTS] = 0
            prefs[KEY_LOCKOUT_UNTIL] = 0L
        }
    }

    /** Clear all preferences (for factory reset) */
    suspend fun clearAll() {
        context.securityDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
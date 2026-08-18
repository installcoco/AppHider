package com.apphider.data.repository

import com.apphider.data.local.crypt.KeystoreManager
import com.apphider.data.local.datastore.SecurityPreferences
import com.apphider.domain.repository.SecurityRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [SecurityRepository] that manages password operations
 * using Android Keystore for encryption and DataStore for persistence.
 */
@Singleton
class SecurityRepositoryImpl @Inject constructor(
    private val keystoreManager: KeystoreManager,
    private val securityPreferences: SecurityPreferences
) : SecurityRepository {

    override val isSetupComplete: Flow<Boolean> = securityPreferences.isSetupCompleteFlow
    override val failedAttempts: Flow<Int> = securityPreferences.failedAttemptsFlow
    override val lockoutUntil: Flow<Long> = securityPreferences.lockoutUntilFlow
    override val disguiseTheme: Flow<String> = securityPreferences.disguiseThemeFlow
    override val biometricEnabled: Flow<Boolean> = securityPreferences.biometricEnabledFlow

    override suspend fun setPassword(password: String): Result<Unit> {
        return try {
            val (encrypted, iv) = keystoreManager.encrypt(password)
            securityPreferences.setEncryptedPassword(encrypted, iv)
            securityPreferences.setSetupComplete(true)
            securityPreferences.resetFailedAttempts()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyPassword(password: String): Boolean {
        // Check lockout
        val currentLockout = getCurrentLockoutUntil()
        if (currentLockout > System.currentTimeMillis()) {
            return false
        }

        // Read encrypted password
        val encryptedPassword = getCurrentEncryptedPassword() ?: return false
        val iv = getCurrentIv() ?: return false

        val decrypted = keystoreManager.decrypt(encryptedPassword, iv)
        val isValid = decrypted == password

        if (isValid) {
            securityPreferences.resetFailedAttempts()
        } else {
            val currentAttempts = getCurrentFailedAttempts()
            val newAttempts = currentAttempts + 1
            securityPreferences.setFailedAttempts(newAttempts)
            if (newAttempts >= SecurityPreferences.MAX_FAILED_ATTEMPTS) {
                securityPreferences.setLockoutUntil(
                    System.currentTimeMillis() + SecurityPreferences.LOCKOUT_DURATION_MS
                )
            }
        }

        return isValid
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        if (!verifyPassword(oldPassword)) {
            return Result.failure(Exception("Old password is incorrect"))
        }
        return setPassword(newPassword)
    }

    override suspend fun resetPassword(): Result<Unit> {
        return try {
            keystoreManager.deleteKey()
            securityPreferences.clearAll()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isLockedOut(): Boolean {
        val lockoutUntil = getCurrentLockoutUntil()
        val now = System.currentTimeMillis()
        if (lockoutUntil > now) {
            return true
        }
        if (lockoutUntil > 0 && lockoutUntil <= now) {
            // Lockout expired, reset
            securityPreferences.resetFailedAttempts()
        }
        return false
    }

    override suspend fun getRemainingLockoutSeconds(): Int {
        val lockoutUntil = getCurrentLockoutUntil()
        val remaining = (lockoutUntil - System.currentTimeMillis()) / 1000
        return if (remaining > 0) remaining.toInt() else 0
    }

    override suspend fun setDisguiseTheme(theme: String) {
        securityPreferences.setDisguiseTheme(theme)
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        securityPreferences.setBiometricEnabled(enabled)
    }

    // Helper methods for one-shot reads from DataStore using Flow.first()
    private suspend fun getCurrentEncryptedPassword(): String? {
        return try {
            securityPreferences.encryptedPasswordFlow.first()
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getCurrentIv(): String? {
        return try {
            securityPreferences.passwordIvFlow.first()
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getCurrentFailedAttempts(): Int {
        return try {
            securityPreferences.failedAttemptsFlow.first()
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun getCurrentLockoutUntil(): Long {
        return try {
            securityPreferences.lockoutUntilFlow.first()
        } catch (e: Exception) {
            0L
        }
    }
}
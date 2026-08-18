package com.apphider.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for password and security operations.
 */
interface SecurityRepository {

    /** Whether the initial password setup is complete */
    val isSetupComplete: Flow<Boolean>

    /** Number of consecutive failed password attempts */
    val failedAttempts: Flow<Int>

    /** Timestamp until which the app is locked */
    val lockoutUntil: Flow<Long>

    /** Current disguise theme identifier */
    val disguiseTheme: Flow<String>

    /** Whether biometric authentication is enabled */
    val biometricEnabled: Flow<Boolean>

    /**
     * Encrypts and stores the initial password.
     */
    suspend fun setPassword(password: String): Result<Unit>

    /**
     * Verifies the given password against the stored encrypted password.
     * Manages failed attempt counting and lockout.
     */
    suspend fun verifyPassword(password: String): Boolean

    /**
     * Changes the password after verifying the old password.
     */
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit>

    /**
     * Resets the password (clears all security preferences).
     */
    suspend fun resetPassword(): Result<Unit>

    /**
     * Checks if the app is currently locked due to too many failed attempts.
     */
    suspend fun isLockedOut(): Boolean

    /**
     * Returns the remaining lockout time in seconds.
     */
    suspend fun getRemainingLockoutSeconds(): Int

    /**
     * Sets the disguise theme (calculator, notes, weather).
     */
    suspend fun setDisguiseTheme(theme: String)

    /**
     * Enables or disables biometric authentication.
     */
    suspend fun setBiometricEnabled(enabled: Boolean)
}
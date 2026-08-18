package com.apphider.domain.usecase

import com.apphider.domain.repository.SecurityRepository
import javax.inject.Inject

/**
 * Use case for verifying the access password.
 * Handles lockout state checking.
 */
class VerifyPasswordUseCase @Inject constructor(
    private val securityRepository: SecurityRepository
) {
    /**
     * Verifies the password. Returns true if valid, false otherwise.
     * Throws an exception if the app is currently locked out.
     */
    suspend operator fun invoke(password: String): Result<Boolean> {
        if (securityRepository.isLockedOut()) {
            val remaining = securityRepository.getRemainingLockoutSeconds()
            return Result.failure(Exception("Locked out. Remaining: ${remaining}s"))
        }
        val isValid = securityRepository.verifyPassword(password)
        return Result.success(isValid)
    }
}
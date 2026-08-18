package com.apphider.domain.usecase

import com.apphider.domain.repository.SecurityRepository
import javax.inject.Inject

/**
 * Use case for setting the initial password.
 * Validates password length (4-6 digits).
 */
class SetPasswordUseCase @Inject constructor(
    private val securityRepository: SecurityRepository
) {
    suspend operator fun invoke(password: String): Result<Unit> {
        if (password.length !in 4..6 || !password.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("Password must be 4-6 digits"))
        }
        return securityRepository.setPassword(password)
    }
}
package com.apphider.domain.usecase

import com.apphider.domain.repository.AppRepository
import javax.inject.Inject

/**
 * Use case for unhiding an application.
 */
class UnhideAppUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    suspend operator fun invoke(packageName: String): Result<Unit> {
        return appRepository.unhideApp(packageName)
    }
}
package com.apphider.domain.usecase

import com.apphider.domain.repository.AppRepository
import javax.inject.Inject

/**
 * Use case for hiding an application.
 */
class HideAppUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    suspend operator fun invoke(packageName: String, appName: String): Result<Unit> {
        return appRepository.hideApp(packageName, appName)
    }
}
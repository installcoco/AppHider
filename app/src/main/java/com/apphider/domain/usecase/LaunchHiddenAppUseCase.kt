package com.apphider.domain.usecase

import com.apphider.domain.repository.AppRepository
import javax.inject.Inject

/**
 * Use case for launching a hidden application from within the hidden space.
 */
class LaunchHiddenAppUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    suspend operator fun invoke(packageName: String): Result<Unit> {
        return appRepository.launchApp(packageName)
    }
}
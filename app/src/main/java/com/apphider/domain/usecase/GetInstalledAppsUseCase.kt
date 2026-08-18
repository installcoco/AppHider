package com.apphider.domain.usecase

import com.apphider.domain.model.AppInfo
import com.apphider.domain.repository.AppRepository
import javax.inject.Inject

/**
 * Use case for retrieving all installed third-party applications.
 */
class GetInstalledAppsUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    suspend operator fun invoke(): List<AppInfo> {
        return appRepository.getInstalledThirdPartyApps()
    }
}
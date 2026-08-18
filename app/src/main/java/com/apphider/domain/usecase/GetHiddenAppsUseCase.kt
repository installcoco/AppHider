package com.apphider.domain.usecase

import com.apphider.domain.model.HiddenAppInfo
import com.apphider.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for observing the list of hidden applications.
 */
class GetHiddenAppsUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    operator fun invoke(): Flow<List<HiddenAppInfo>> {
        return appRepository.getHiddenApps()
    }
}
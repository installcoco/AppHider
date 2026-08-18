package com.apphider.ui.hidden

import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apphider.domain.model.HiddenAppInfo
import com.apphider.domain.repository.AppRepository
import com.apphider.domain.usecase.GetHiddenAppsUseCase
import com.apphider.domain.usecase.LaunchHiddenAppUseCase
import com.apphider.domain.usecase.UnhideAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the hidden space screen.
 */
data class HiddenSpaceUiState(
    val hiddenApps: List<HiddenAppInfo> = emptyList(),
    val appIcons: Map<String, Drawable?> = emptyMap(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * ViewModel for the hidden space screen.
 * Manages the list of hidden apps and actions like launching and unhiding.
 */
@HiltViewModel
class HiddenSpaceViewModel @Inject constructor(
    private val getHiddenAppsUseCase: GetHiddenAppsUseCase,
    private val launchHiddenAppUseCase: LaunchHiddenAppUseCase,
    private val unhideAppUseCase: UnhideAppUseCase,
    private val appRepository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HiddenSpaceUiState())
    val uiState: StateFlow<HiddenSpaceUiState> = _uiState.asStateFlow()

    init {
        loadHiddenApps()
    }

    private fun loadHiddenApps() {
        viewModelScope.launch {
            getHiddenAppsUseCase().collect { apps ->
                _uiState.update { it.copy(hiddenApps = apps, isLoading = false) }
                // Load icons for all hidden apps
                apps.forEach { app ->
                    viewModelScope.launch {
                        val icon = appRepository.getHiddenAppIcon(app.packageName)
                        _uiState.update { state ->
                            val newIcons = state.appIcons.toMutableMap()
                            newIcons[app.packageName] = icon
                            state.copy(appIcons = newIcons)
                        }
                    }
                }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadHiddenApps()
    }

    fun launchApp(packageName: String) {
        viewModelScope.launch {
            val result = launchHiddenAppUseCase(packageName)
            result.fold(
                onSuccess = { /* app launched */ },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
            )
        }
    }

    fun unhideApp(packageName: String) {
        viewModelScope.launch {
            val result = unhideAppUseCase(packageName)
            result.fold(
                onSuccess = { /* app unhidden, list will auto-update via Flow */ },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
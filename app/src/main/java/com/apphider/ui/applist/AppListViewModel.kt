package com.apphider.ui.applist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apphider.domain.model.AppInfo
import com.apphider.domain.usecase.GetInstalledAppsUseCase
import com.apphider.domain.usecase.HideAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the app list screen.
 */
data class AppListUiState(
    val apps: List<AppInfo> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isSelectionMode: Boolean = false,
    val selectedCount: Int = 0,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel for the app list selection screen.
 * Handles searching, selecting, and hiding applications.
 */
@HiltViewModel
class AppListViewModel @Inject constructor(
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val hideAppUseCase: HideAppUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppListUiState())
    val uiState: StateFlow<AppListUiState> = _uiState.asStateFlow()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val apps = getInstalledAppsUseCase()
                _uiState.update {
                    it.copy(
                        apps = apps,
                        filteredApps = apps,
                        isLoading = false,
                        isSelectionMode = false,
                        selectedCount = 0
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "加载应用列表失败: ${e.message}")
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isBlank()) {
                state.apps
            } else {
                state.apps.filter { app ->
                    app.appName.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)
                }
            }
            state.copy(searchQuery = query, filteredApps = filtered)
        }
    }

    fun toggleAppSelection(packageName: String) {
        _uiState.update { state ->
            val updatedApps = state.apps.map { app ->
                if (app.packageName == packageName) {
                    app.copy(isSelected = !app.isSelected)
                } else {
                    app
                }
            }
            val selectedCount = updatedApps.count { it.isSelected }
            val updatedFiltered = state.filteredApps.map { app ->
                if (app.packageName == packageName) {
                    app.copy(isSelected = !app.isSelected)
                } else {
                    app
                }
            }
            state.copy(
                apps = updatedApps,
                filteredApps = updatedFiltered,
                isSelectionMode = selectedCount > 0,
                selectedCount = selectedCount
            )
        }
    }

    fun selectAll() {
        _uiState.update { state ->
            val updatedApps = state.apps.map { it.copy(isSelected = true) }
            val updatedFiltered = state.filteredApps.map { it.copy(isSelected = true) }
            val count = updatedApps.size
            state.copy(apps = updatedApps, filteredApps = updatedFiltered, isSelectionMode = true, selectedCount = count)
        }
    }

    fun deselectAll() {
        _uiState.update { state ->
            val updatedApps = state.apps.map { it.copy(isSelected = false) }
            val updatedFiltered = state.filteredApps.map { it.copy(isSelected = false) }
            state.copy(apps = updatedApps, filteredApps = updatedFiltered, isSelectionMode = false, selectedCount = 0)
        }
    }

    fun hideSelectedApps() {
        viewModelScope.launch {
            val selectedApps = _uiState.value.apps.filter { it.isSelected }
            var successCount = 0
            var failCount = 0

            for (app in selectedApps) {
                val result = hideAppUseCase(app.packageName, app.appName)
                if (result.isSuccess) {
                    successCount++
                } else {
                    failCount++
                }
            }

            if (failCount > 0) {
                _uiState.update {
                    it.copy(
                        errorMessage = "隐藏完成: $successCount 成功, $failCount 失败",
                        selectedCount = 0,
                        isSelectionMode = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        successMessage = "成功隐藏 $successCount 个应用",
                        selectedCount = 0,
                        isSelectionMode = false
                    )
                }
            }

            // Reload to update hidden status
            loadApps()
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
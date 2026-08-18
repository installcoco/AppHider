package com.apphider.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apphider.domain.repository.AppRepository
import com.apphider.domain.repository.SecurityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the settings screen.
 */
data class SettingsUiState(
    val currentTheme: String = "calculator",
    val biometricEnabled: Boolean = false,
    val showChangePasswordDialog: Boolean = false,
    val showConfirmUnhideAll: Boolean = false,
    val showAboutDialog: Boolean = false,
    val showDisclaimerDialog: Boolean = false,
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmNewPassword: String = "",
    val passwordError: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel for the settings screen.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val securityRepository: SecurityRepository,
    private val appRepository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            securityRepository.disguiseTheme.collect { theme ->
                _uiState.update { it.copy(currentTheme = theme) }
            }
        }
        viewModelScope.launch {
            securityRepository.biometricEnabled.collect { enabled ->
                _uiState.update { it.copy(biometricEnabled = enabled) }
            }
        }
    }

    fun onThemeChange(theme: String) {
        viewModelScope.launch {
            securityRepository.setDisguiseTheme(theme)
            _uiState.update { it.copy(currentTheme = theme) }
        }
    }

    fun onBiometricToggle(enabled: Boolean) {
        viewModelScope.launch {
            securityRepository.setBiometricEnabled(enabled)
            _uiState.update { it.copy(biometricEnabled = enabled) }
        }
    }

    fun showChangePassword() {
        _uiState.update { it.copy(
            showChangePasswordDialog = true,
            oldPassword = "",
            newPassword = "",
            confirmNewPassword = "",
            passwordError = null
        )}
    }

    fun hideChangePassword() {
        _uiState.update { it.copy(showChangePasswordDialog = false, passwordError = null) }
    }

    fun onOldPasswordChange(value: String) = _uiState.update { it.copy(oldPassword = value) }
    fun onNewPasswordChange(value: String) = _uiState.update { it.copy(newPassword = value) }
    fun onConfirmNewPasswordChange(value: String) = _uiState.update { it.copy(confirmNewPassword = value) }

    fun changePassword() {
        val state = _uiState.value
        if (state.newPassword.length !in 4..6 || !state.newPassword.all { it.isDigit() }) {
            _uiState.update { it.copy(passwordError = "新密码需为 4-6 位数字") }
            return
        }
        if (state.newPassword != state.confirmNewPassword) {
            _uiState.update { it.copy(passwordError = "两次密码不一致") }
            return
        }
        viewModelScope.launch {
            val result = securityRepository.changePassword(state.oldPassword, state.newPassword)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(
                        showChangePasswordDialog = false,
                        passwordError = null,
                        successMessage = "密码修改成功"
                    )}
                },
                onFailure = { error ->
                    _uiState.update { it.copy(passwordError = error.message ?: "修改失败") }
                }
            )
        }
    }

    fun showConfirmUnhideAll() {
        _uiState.update { it.copy(showConfirmUnhideAll = true) }
    }

    fun hideConfirmUnhideAll() {
        _uiState.update { it.copy(showConfirmUnhideAll = false) }
    }

    fun unhideAllApps() {
        viewModelScope.launch {
            val result = appRepository.unhideAllApps()
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(
                        showConfirmUnhideAll = false,
                        successMessage = "已取消隐藏所有应用"
                    )}
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        showConfirmUnhideAll = false,
                        passwordError = "操作失败: ${error.message}"
                    )}
                }
            )
        }
    }

    fun showAbout() {
        _uiState.update { it.copy(showAboutDialog = true) }
    }

    fun hideAbout() {
        _uiState.update { it.copy(showAboutDialog = false) }
    }

    fun showDisclaimer() {
        _uiState.update { it.copy(showDisclaimerDialog = true) }
    }

    fun hideDisclaimer() {
        _uiState.update { it.copy(showDisclaimerDialog = false) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, passwordError = null) }
    }
}
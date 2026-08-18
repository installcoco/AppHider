package com.apphider.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apphider.domain.usecase.SetPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the setup screen.
 */
data class SetupUiState(
    val step: SetupStep = SetupStep.ENTER_PASSWORD,
    val password: String = "",
    val confirmPassword: String = "",
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

enum class SetupStep {
    ENTER_PASSWORD,
    CONFIRM_PASSWORD
}

/**
 * ViewModel for the initial password setup screen.
 */
@HiltViewModel
class SetupViewModel @Inject constructor(
    private val setPasswordUseCase: SetPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    fun onDigitClick(digit: String) {
        val state = _uiState.value
        when (state.step) {
            SetupStep.ENTER_PASSWORD -> {
                if (state.password.length < 6) {
                    val newPassword = state.password + digit
                    _uiState.update { it.copy(password = newPassword, errorMessage = null) }
                }
            }
            SetupStep.CONFIRM_PASSWORD -> {
                if (state.confirmPassword.length < 6) {
                    val newConfirm = state.confirmPassword + digit
                    _uiState.update { it.copy(confirmPassword = newConfirm, errorMessage = null) }
                }
            }
        }
    }

    fun onDeleteClick() {
        val state = _uiState.value
        when (state.step) {
            SetupStep.ENTER_PASSWORD -> {
                if (state.password.isNotEmpty()) {
                    _uiState.update { it.copy(password = state.password.dropLast(1)) }
                }
            }
            SetupStep.CONFIRM_PASSWORD -> {
                if (state.confirmPassword.isNotEmpty()) {
                    _uiState.update { it.copy(confirmPassword = state.confirmPassword.dropLast(1)) }
                }
            }
        }
    }

    fun onNextClick() {
        val state = _uiState.value
        when (state.step) {
            SetupStep.ENTER_PASSWORD -> {
                if (state.password.length < 4) {
                    _uiState.update { it.copy(errorMessage = "密码需 4-6 位数字") }
                    return
                }
                _uiState.update { it.copy(step = SetupStep.CONFIRM_PASSWORD, errorMessage = null) }
            }
            SetupStep.CONFIRM_PASSWORD -> {
                if (state.password != state.confirmPassword) {
                    _uiState.update { it.copy(errorMessage = "两次密码不一致") }
                    return
                }
                savePassword(state.password)
            }
        }
    }

    private fun savePassword(password: String) {
        viewModelScope.launch {
            val result = setPasswordUseCase(password)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSuccess = true, errorMessage = null) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = error.message ?: "设置失败") }
                }
            )
        }
    }
}
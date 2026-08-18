package com.apphider.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apphider.domain.repository.SecurityRepository
import com.apphider.domain.usecase.VerifyPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the calculator screen.
 */
data class CalculatorUiState(
    val displayText: String = "0",
    val expression: String = "",
    val isPasswordMode: Boolean = false,
    val passwordBuffer: String = "",
    val passwordError: String? = null,
    val isLockedOut: Boolean = false,
    val lockoutRemainingSeconds: Int = 0,
    val titleTapCount: Int = 0,
    val showPasswordDialog: Boolean = false,
    val isAuthenticated: Boolean = false
)

/**
 * ViewModel for the calculator/disguise screen.
 * Handles both calculator logic and password entry for hidden space access.
 */
@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val securityRepository: SecurityRepository,
    private val verifyPasswordUseCase: VerifyPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private var currentValue = 0.0
    private var previousValue = 0.0
    private var currentOperator: String? = null
    private var isNewInput = true
    private var hasDecimal = false

    init {
        // Check if password was just entered correctly
        viewModelScope.launch {
            securityRepository.isSetupComplete.first { it } // Wait for setup
        }
    }

    fun onDigitClick(digit: String) {
        val state = _uiState.value
        if (state.isPasswordMode) {
            // In password mode, digits go to password buffer
            appendPasswordDigit(digit)
            return
        }
        // Normal calculator digit input
        if (isNewInput) {
            currentValue = digit.toDouble()
            isNewInput = false
            hasDecimal = false
            updateDisplay()
        } else {
            val currentText = _uiState.value.displayText
            if (currentText.length < 12) { // Limit display length
                val newText = if (currentText == "0") digit else currentText + digit
                currentValue = newText.toDouble()
                updateDisplay(newText)
            }
        }
    }

    fun onOperatorClick(operator: String) {
        if (_uiState.value.isPasswordMode) return

        if (currentOperator != null && !isNewInput) {
            calculate()
        }
        previousValue = currentValue
        currentOperator = operator
        isNewInput = true
        _uiState.update { it.copy(expression = "${formatNumber(previousValue)} $operator") }
    }

    fun onEqualsClick() {
        val state = _uiState.value
        if (state.isPasswordMode) {
            // In password mode, verify the password
            val password = state.passwordBuffer
            if (password.isNotEmpty()) {
                verifyPassword(password)
            }
            return
        }
        // Normal calculator equals
        if (currentOperator != null) {
            calculate()
            currentOperator = null
            _uiState.update { it.copy(expression = "") }
        }
    }

    fun onClearClick() {
        if (_uiState.value.isPasswordMode) {
            _uiState.update { it.copy(passwordBuffer = "", passwordError = null) }
            return
        }
        currentValue = 0.0
        previousValue = 0.0
        currentOperator = null
        isNewInput = true
        hasDecimal = false
        _uiState.update { it.copy(displayText = "0", expression = "") }
    }

    fun onDeleteClick() {
        val state = _uiState.value
        if (state.isPasswordMode) {
            if (state.passwordBuffer.isNotEmpty()) {
                _uiState.update { it.copy(
                    passwordBuffer = state.passwordBuffer.dropLast(1),
                    passwordError = null
                )}
            }
            return
        }
        // Normal calculator delete
        if (!isNewInput) {
            val currentText = _uiState.value.displayText
            if (currentText.length > 1) {
                val newText = currentText.dropLast(1)
                currentValue = newText.toDoubleOrNull() ?: 0.0
                updateDisplay(newText)
            } else {
                currentValue = 0.0
                isNewInput = true
                updateDisplay("0")
            }
        }
    }

    fun onDotClick() {
        if (_uiState.value.isPasswordMode) return
        if (!hasDecimal) {
            val currentText = _uiState.value.displayText
            val newText = "$currentText."
            currentValue = newText.toDoubleOrNull() ?: currentValue
            hasDecimal = true
            updateDisplay(newText)
        }
    }

    fun onNegateClick() {
        if (_uiState.value.isPasswordMode) return
        currentValue = -currentValue
        updateDisplay()
    }

    fun onPercentClick() {
        if (_uiState.value.isPasswordMode) return
        currentValue /= 100
        updateDisplay()
    }

    /**
     * Called when the calculator title is tapped (for hidden entry via 5 taps).
     */
    fun onTitleTap() {
        val newCount = _uiState.value.titleTapCount + 1
        if (newCount >= 5) {
            _uiState.update { it.copy(
                titleTapCount = 0,
                showPasswordDialog = true,
                isPasswordMode = true
            )}
        } else {
            _uiState.update { it.copy(titleTapCount = newCount) }
        }
    }

    fun onPasswordDialogDismiss() {
        _uiState.update { it.copy(
            showPasswordDialog = false,
            isPasswordMode = false,
            passwordBuffer = "",
            passwordError = null,
            titleTapCount = 0
        )}
    }

    /**
     * Called when the PasswordDialog completes with a password.
     */
    fun verifyPasswordFromDialog(password: String) {
        verifyPassword(password)
    }

    fun onAuthenticated() {
        _uiState.update { it.copy(isAuthenticated = true) }
    }

    fun onNavigatedToHiddenSpace() {
        _uiState.update { it.copy(isAuthenticated = false, showPasswordDialog = false) }
    }

    private fun appendPasswordDigit(digit: String) {
        val state = _uiState.value
        if (state.passwordBuffer.length < 6) {
            _uiState.update { it.copy(
                passwordBuffer = state.passwordBuffer + digit,
                passwordError = null
            )}
        }
    }

    private fun verifyPassword(password: String) {
        viewModelScope.launch {
            val result = verifyPasswordUseCase(password)
            result.fold(
                onSuccess = { isValid ->
                    if (isValid) {
                        _uiState.update { it.copy(
                            passwordBuffer = "",
                            passwordError = null,
                            isPasswordMode = false,
                            showPasswordDialog = false,
                            isAuthenticated = true
                        )}
                    } else {
                        _uiState.update { it.copy(
                            passwordBuffer = "",
                            passwordError = "密码错误"
                        )}
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        passwordBuffer = "",
                        passwordError = error.message ?: "验证失败"
                    )}
                }
            )
        }
    }

    private fun calculate() {
        if (currentOperator == null) return
        when (currentOperator) {
            "+" -> currentValue = previousValue + currentValue
            "-" -> currentValue = previousValue - currentValue
            "×" -> currentValue = previousValue * currentValue
            "÷" -> {
                if (currentValue != 0.0) {
                    currentValue = previousValue / currentValue
                } else {
                    _uiState.update { it.copy(displayText = "Error") }
                    return
                }
            }
        }
        isNewInput = true
        updateDisplay()
    }

    private fun updateDisplay(text: String? = null) {
        _uiState.update { it.copy(displayText = text ?: formatNumber(currentValue)) }
    }

    private fun formatNumber(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format("%.8f", value).trimEnd('0').trimEnd('.')
        }
    }
}
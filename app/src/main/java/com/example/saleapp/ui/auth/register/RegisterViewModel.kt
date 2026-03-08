package com.example.saleapp.ui.auth.register

import androidx.lifecycle.viewModelScope
import com.example.saleapp.core.base.BaseViewModel
import com.example.saleapp.core.network.NetworkResult
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.data.model.response.UserResponse
import com.example.saleapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel() {

    private val _registerState = MutableStateFlow<UiState<UserResponse>>(UiState.Idle)
    val registerState: StateFlow<UiState<UserResponse>> = _registerState

    fun register(username: String, email: String, password: String, phoneNumber: String?, address: String?) {
        if (username.isBlank() || password.isBlank()) {
            _registerState.value = UiState.Error("Username and password are required")
            return
        }

        viewModelScope.launch(exceptionHandler) {
            _registerState.value = UiState.Loading
            when (val result = authRepository.register(username, email, password, phoneNumber, address)) {
                is NetworkResult.Success -> _registerState.value = UiState.Success(result.data)
                is NetworkResult.Error -> _registerState.value = UiState.Error(result.message ?: "Registration failed", result.code)
                is NetworkResult.Exception -> _registerState.value = UiState.Error(result.e.message ?: "Unknown error")
            }
        }
    }
}


package com.example.saleapp.ui.auth.login

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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel() {

    private val _loginState = MutableStateFlow<UiState<UserResponse>>(UiState.Idle)
    val loginState: StateFlow<UiState<UserResponse>> = _loginState

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _loginState.value = UiState.Error("Username and password cannot be empty")
            return
        }

        viewModelScope.launch(exceptionHandler) {
            _loginState.value = UiState.Loading
            when (val result = authRepository.login(username, password)) {
                is NetworkResult.Success -> _loginState.value = UiState.Success(result.data)
                is NetworkResult.Error -> _loginState.value = UiState.Error(result.message ?: "Login failed", result.code)
                is NetworkResult.Exception -> _loginState.value = UiState.Error(result.e.message ?: "Unknown error")
            }
        }
    }
}


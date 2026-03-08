package com.example.saleapp.ui.profile

import androidx.lifecycle.viewModelScope
import com.example.saleapp.core.base.BaseViewModel
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.data.model.response.UserResponse
import com.example.saleapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel() {

    private val _profileState = MutableStateFlow<UiState<UserResponse>>(UiState.Idle)
    val profileState: StateFlow<UiState<UserResponse>> = _profileState

    private val _logoutState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val logoutState: StateFlow<UiState<Unit>> = _logoutState

    fun loadProfile() {
        _profileState.value = UiState.Loading
        viewModelScope.launch(exceptionHandler) {
            when (val result = authRepository.getCurrentUser()) {
                is com.example.saleapp.core.network.NetworkResult.Success -> {
                    _profileState.value = UiState.Success(result.data)
                }
                is com.example.saleapp.core.network.NetworkResult.Error -> {
                    _profileState.value = UiState.Error(result.message ?: "Failed to load profile")
                }
                is com.example.saleapp.core.network.NetworkResult.Exception -> {
                    _profileState.value = UiState.Error(result.e.message ?: "Unknown error")
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch(exceptionHandler) {
            _logoutState.value = UiState.Loading
            authRepository.logout()
            _logoutState.value = UiState.Success(Unit)
        }
    }
}


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
        _profileState.value = UiState.Idle
    }

    fun logout() {
        viewModelScope.launch(exceptionHandler) {
            _logoutState.value = UiState.Loading
            authRepository.logout()
            _logoutState.value = UiState.Success(Unit)
        }
    }
}


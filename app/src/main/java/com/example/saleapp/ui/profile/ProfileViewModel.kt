package com.example.saleapp.ui.profile

import androidx.lifecycle.viewModelScope
import com.example.saleapp.core.base.BaseViewModel
import com.example.saleapp.core.network.NetworkResult
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.data.model.response.PagedOrderResponse
import com.example.saleapp.data.model.response.UserResponse
import com.example.saleapp.data.repository.AuthRepository
import com.example.saleapp.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository
) : BaseViewModel() {

    private val _profileState = MutableStateFlow<UiState<UserResponse>>(UiState.Idle)
    val profileState: StateFlow<UiState<UserResponse>> = _profileState

    private val _logoutState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val logoutState: StateFlow<UiState<Unit>> = _logoutState

    private val _ordersState = MutableStateFlow<UiState<PagedOrderResponse>>(UiState.Idle)
    val ordersState: StateFlow<UiState<PagedOrderResponse>> = _ordersState

    /** Current page (1-based, matching backend) */
    private var currentPage = 1
    private val pageSize = 10

    fun loadProfile() {
        _profileState.value = UiState.Loading
        viewModelScope.launch(exceptionHandler) {
            when (val result = authRepository.getCurrentUser()) {
                is NetworkResult.Success -> _profileState.value = UiState.Success(result.data)
                is NetworkResult.Error   -> _profileState.value = UiState.Error(result.message ?: "Failed to load profile")
                is NetworkResult.Exception -> _profileState.value = UiState.Error(result.e.message ?: "Unknown error")
            }
        }
    }

    fun loadOrders(page: Int = currentPage) {
        currentPage = page
        _ordersState.value = UiState.Loading
        viewModelScope.launch(exceptionHandler) {
            when (val result = orderRepository.getMyOrders(page, pageSize)) {
                is NetworkResult.Success   -> _ordersState.value = UiState.Success(result.data)
                is NetworkResult.Error     -> _ordersState.value = UiState.Error(result.message ?: "Không thể tải đơn hàng")
                is NetworkResult.Exception -> _ordersState.value = UiState.Error(result.e.message ?: "Lỗi kết nối")
            }
        }
    }

    fun nextPage(totalPages: Int) {
        if (currentPage < totalPages) loadOrders(currentPage + 1)
    }

    fun prevPage() {
        if (currentPage > 1) loadOrders(currentPage - 1)
    }

    fun logout() {
        viewModelScope.launch(exceptionHandler) {
            _logoutState.value = UiState.Loading
            authRepository.logout()
            _logoutState.value = UiState.Success(Unit)
        }
    }

    override fun handleError(throwable: Throwable) {
        _profileState.value = UiState.Error(throwable.message ?: "Unknown error")
    }
}



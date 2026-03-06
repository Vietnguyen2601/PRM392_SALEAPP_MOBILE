package com.example.saleapp.ui.cart

import androidx.lifecycle.viewModelScope
import com.example.saleapp.core.base.BaseViewModel
import com.example.saleapp.core.network.NetworkResult
import com.example.saleapp.core.notification.CartBadgeManager
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.data.model.response.CartResponse
import com.example.saleapp.data.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val cartBadgeManager: CartBadgeManager
) : BaseViewModel() {

    private val _cartState = MutableStateFlow<UiState<CartResponse>>(UiState.Idle)
    val cartState: StateFlow<UiState<CartResponse>> = _cartState

    private val _removeItemState = MutableStateFlow<UiState<CartResponse>>(UiState.Idle)
    val removeItemState: StateFlow<UiState<CartResponse>> = _removeItemState

    fun loadCart() {
        viewModelScope.launch(exceptionHandler) {
            _cartState.value = UiState.Loading
            when (val result = cartRepository.getCart()) {
                is NetworkResult.Success -> {
                    _cartState.value = UiState.Success(result.data)
                    // Update cart badge notification
                    cartBadgeManager.updateCartBadge(result.data.getTotalItems())
                }
                is NetworkResult.Error -> _cartState.value = UiState.Error(result.message ?: "Failed to load cart", result.code)
                is NetworkResult.Exception -> _cartState.value = UiState.Error(result.e.message ?: "Unknown error")
            }
        }
    }

    fun removeItem(itemId: Long) {
        viewModelScope.launch(exceptionHandler) {
            _removeItemState.value = UiState.Loading
            when (val result = cartRepository.removeFromCart(itemId)) {
                is NetworkResult.Success -> {
                    _removeItemState.value = UiState.Success(result.data)
                    _cartState.value = UiState.Success(result.data)
                    // Update cart badge notification
                    cartBadgeManager.updateCartBadge(result.data.getTotalItems())
                }
                is NetworkResult.Error -> _removeItemState.value = UiState.Error(result.message ?: "Failed to remove item", result.code)
                is NetworkResult.Exception -> _removeItemState.value = UiState.Error(result.e.message ?: "Unknown error")
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch(exceptionHandler) {
            cartRepository.clearCart()
            loadCart()
        }
    }
}


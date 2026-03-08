package com.example.saleapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saleapp.core.network.NetworkResult
import com.example.saleapp.core.notification.CartBadgeManager
import com.example.saleapp.data.model.response.ProductResponse
import com.example.saleapp.data.model.response.CartResponse
import com.example.saleapp.data.repository.ProductRepository
import com.example.saleapp.data.repository.CartRepository
import com.example.saleapp.core.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val cartBadgeManager: CartBadgeManager
) : ViewModel() {

    private val _productState: MutableStateFlow<UiState<ProductResponse>> = MutableStateFlow(UiState.Idle)
    val productState: StateFlow<UiState<ProductResponse>> = _productState

    private val _addToCartState: MutableStateFlow<UiState<CartResponse>> = MutableStateFlow(UiState.Idle)
    val addToCartState: StateFlow<UiState<CartResponse>> = _addToCartState

    fun loadProductDetails(productId: Long) {
        viewModelScope.launch {
            _productState.value = UiState.Loading
            when (val result = productRepository.getProductById(productId)) {
                is NetworkResult.Success -> {
                    _productState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _productState.value = UiState.Error(result.message ?: "Unknown error")
                }
                is NetworkResult.Exception -> {
                    _productState.value = UiState.Error(result.e.message ?: "Unknown error")
                }
            }
        }
    }

    fun addToCart(productId: Long, quantity: Int = 1) {
        viewModelScope.launch {
            android.util.Log.d("ProductDetailVM", "addToCart called: productId=$productId, quantity=$quantity")
            _addToCartState.value = UiState.Loading
            when (val result = cartRepository.addToCart(productId, quantity)) {
                is NetworkResult.Success -> {
                    android.util.Log.d("ProductDetailVM", "addToCart success: ${result.data}")
                    _addToCartState.value = UiState.Success(result.data)
                    // Update cart badge notification
                    cartBadgeManager.updateCartBadge(result.data.getTotalItems())
                }
                is NetworkResult.Error -> {
                    android.util.Log.e("ProductDetailVM", "addToCart error: ${result.message}")
                    _addToCartState.value = UiState.Error(result.message ?: "Failed to add to cart", result.code)
                }
                is NetworkResult.Exception -> {
                    android.util.Log.e("ProductDetailVM", "addToCart exception: ${result.e.message}", result.e)
                    _addToCartState.value = UiState.Error(result.e.message ?: "Unknown error")
                }
            }
        }
    }
}


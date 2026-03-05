package com.example.saleapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saleapp.core.network.NetworkResult
import com.example.saleapp.data.model.response.ProductResponse
import com.example.saleapp.data.repository.ProductRepository
import com.example.saleapp.core.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _productState: MutableStateFlow<UiState<ProductResponse>> = MutableStateFlow(UiState.Idle)
    val productState: StateFlow<UiState<ProductResponse>> = _productState

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
}


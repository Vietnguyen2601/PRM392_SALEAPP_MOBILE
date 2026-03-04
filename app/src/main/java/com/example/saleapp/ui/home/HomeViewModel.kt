package com.example.saleapp.ui.home

import androidx.lifecycle.viewModelScope
import com.example.saleapp.core.base.BaseViewModel
import com.example.saleapp.core.network.NetworkResult
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.data.model.response.ProductResponse
import com.example.saleapp.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : BaseViewModel() {

    private val _productsState = MutableStateFlow<UiState<List<ProductResponse>>>(UiState.Idle)
    val productsState: StateFlow<UiState<List<ProductResponse>>> = _productsState

    fun loadProducts(
        page: Int = 0,
        keyword: String? = null,
        categoryId: Long? = null,
        sort: String? = null
    ) {
        viewModelScope.launch(exceptionHandler) {
            _productsState.value = UiState.Loading
            when (val result = productRepository.getProducts(page, keyword = keyword, categoryId = categoryId, sort = sort)) {
                is NetworkResult.Success -> _productsState.value = UiState.Success(result.data)
                is NetworkResult.Error -> _productsState.value = UiState.Error(result.message ?: "Failed to load products", result.code)
                is NetworkResult.Exception -> _productsState.value = UiState.Error(result.e.message ?: "Unknown error")
            }
        }
    }
}


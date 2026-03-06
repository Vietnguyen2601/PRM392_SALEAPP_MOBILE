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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : BaseViewModel() {

    private val _allProducts = MutableStateFlow<List<ProductResponse>>(emptyList())
    private val _loadingState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val loadingState: StateFlow<UiState<Unit>> = _loadingState

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState

    // Combined flow: whenever allProducts or filterState changes, recompute filtered list
    val productsState: StateFlow<UiState<List<ProductResponse>>> =
        combine(_allProducts, _filterState, _loadingState) { products, filter, loading ->
            when (loading) {
                is UiState.Loading -> UiState.Loading
                is UiState.Error -> loading
                else -> {
                    val filtered = applyFilterAndSort(products, filter)
                    UiState.Success(filtered)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Idle
        )

    fun loadProducts() {
        viewModelScope.launch(exceptionHandler) {
            _loadingState.value = UiState.Loading
            when (val result = productRepository.getAllProducts()) {
                is NetworkResult.Success -> {
                    _allProducts.value = result.data
                    _loadingState.value = UiState.Success(Unit)
                }
                is NetworkResult.Error -> {
                    _loadingState.value = UiState.Error(result.message ?: "Failed to load products", result.code)
                }
                is NetworkResult.Exception -> {
                    _loadingState.value = UiState.Error(result.e.message ?: "Unknown error")
                }
            }
        }
    }

    fun applyFilter(newFilter: FilterState) {
        _filterState.value = newFilter
    }

    fun updateSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    fun resetFilters() {
        _filterState.value = FilterState()
    }

    private fun applyFilterAndSort(
        products: List<ProductResponse>,
        filter: FilterState
    ): List<ProductResponse> {
        var result = products

        // Search
        if (filter.searchQuery.isNotBlank()) {
            val q = filter.searchQuery.trim().lowercase()
            result = result.filter {
                it.getNameValue().lowercase().contains(q) ||
                        (it.description?.lowercase()?.contains(q) == true) ||
                        (it.categoryName?.lowercase()?.contains(q) == true)
            }
        }

        // Category filter
        if (filter.category != null) {
            result = result.filter {
                it.categoryName?.equals(filter.category, ignoreCase = true) == true
            }
        }

        // Min rating filter
        if (filter.minRating > 0f) {
            result = result.filter { it.getRatingValue() >= filter.minRating }
        }

        // Price range filter
        result = result.filter {
            val price = it.getPriceValue()
            val maxPrice = if (filter.maxPrice >= 1000f) Double.MAX_VALUE else filter.maxPrice.toDouble()
            price >= filter.minPrice && price <= maxPrice
        }

        // Sort
        result = when (filter.sortOption) {
            SortOption.PRICE_ASC -> result.sortedBy { it.getPriceValue() }
            SortOption.PRICE_DESC -> result.sortedByDescending { it.getPriceValue() }
            SortOption.RATING -> result.sortedByDescending { it.getRatingValue() }
            SortOption.POPULAR -> result.sortedByDescending { it.totalSold ?: 0 }
            SortOption.DEFAULT -> result
        }

        return result
    }
}

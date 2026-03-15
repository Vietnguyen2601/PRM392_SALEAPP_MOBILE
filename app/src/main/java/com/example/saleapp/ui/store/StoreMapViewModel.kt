package com.example.saleapp.ui.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saleapp.core.network.NetworkResult
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.data.model.StoreDto
import com.example.saleapp.data.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreMapViewModel @Inject constructor(
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val _storesState = MutableStateFlow<UiState<List<StoreDto>>>(UiState.Idle)
    val storesState: StateFlow<UiState<List<StoreDto>>> = _storesState

    private val _storeDetailState = MutableStateFlow<UiState<StoreDto>>(UiState.Idle)
    val storeDetailState: StateFlow<UiState<StoreDto>> = _storeDetailState

    fun getStores() {
        viewModelScope.launch {
            _storesState.value = UiState.Loading
            when (val result = storeRepository.getStores()) {
                is NetworkResult.Success -> {
                    _storesState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _storesState.value = UiState.Error(result.message ?: "Unknown error")
                }
                is NetworkResult.Exception -> {
                    _storesState.value = UiState.Error(result.e.message ?: "Unknown error")
                }
            }
        }
    }

    fun getStoreDetail(id: Int) {
        viewModelScope.launch {
            _storeDetailState.value = UiState.Loading
            when (val result = storeRepository.getStoreDetail(id)) {
                is NetworkResult.Success -> {
                    _storeDetailState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _storeDetailState.value = UiState.Error(result.message ?: "Unknown error")
                }
                is NetworkResult.Exception -> {
                    _storeDetailState.value = UiState.Error(result.e.message ?: "Unknown error")
                }
            }
        }
    }
}


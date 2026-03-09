package com.example.saleapp.ui.payment

import androidx.lifecycle.viewModelScope
import com.example.saleapp.core.base.BaseViewModel
import com.example.saleapp.core.network.NetworkResult
import com.example.saleapp.core.utils.PreferenceManager
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.data.model.response.PaymentStatusResponse
import com.example.saleapp.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentCallbackViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val preferenceManager: PreferenceManager
) : BaseViewModel() {

    private val _paymentStatusState = MutableStateFlow<UiState<PaymentStatusResponse>>(UiState.Idle)
    val paymentStatusState: StateFlow<UiState<PaymentStatusResponse>> = _paymentStatusState

    /**
     * Verify payment status from backend.
     * This is the source of truth — never trust URL parameters from the deep link.
     */
    fun verifyPayment() {
        viewModelScope.launch(exceptionHandler) {
            _paymentStatusState.value = UiState.Loading

            val paymentId = preferenceManager.getCurrentPaymentId()
            if (paymentId == 0) {
                _paymentStatusState.value = UiState.Error("Không tìm thấy thông tin thanh toán")
                return@launch
            }

            when (val result = paymentRepository.getPaymentStatus(paymentId)) {
                is NetworkResult.Success -> {
                    // Clear saved paymentId after successful verification
                    preferenceManager.clearCurrentPaymentId()
                    _paymentStatusState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _paymentStatusState.value = UiState.Error(
                        result.message ?: "Lỗi xác nhận thanh toán", result.code
                    )
                }
                is NetworkResult.Exception -> {
                    _paymentStatusState.value = UiState.Error(
                        result.e.message ?: "Lỗi kết nối"
                    )
                }
            }
        }
    }

    override fun handleError(throwable: Throwable) {
        _paymentStatusState.value = UiState.Error(throwable.message ?: "Unknown error")
    }
}


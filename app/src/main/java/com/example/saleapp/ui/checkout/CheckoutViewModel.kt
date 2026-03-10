package com.example.saleapp.ui.checkout

import androidx.lifecycle.viewModelScope
import com.example.saleapp.core.base.BaseViewModel
import com.example.saleapp.core.network.NetworkResult
import com.example.saleapp.core.utils.PreferenceManager
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.data.repository.OrderRepository
import com.example.saleapp.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository,
    private val preferenceManager: PreferenceManager
) : BaseViewModel() {

    private val _paymentUrlState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val paymentUrlState: StateFlow<UiState<String>> = _paymentUrlState

    /**
     * Full checkout flow:
     * 1. Create order → get orderId + paymentId
     * 2. Save paymentId for later verification in PaymentCallbackActivity
     * 3. Create VNPay payment URL using orderId
     * 4. Open paymentUrl in browser
     */
    fun checkout(shippingAddress: String, billingAddress: String) {
        viewModelScope.launch(exceptionHandler) {
            _paymentUrlState.value = UiState.Loading

            // Step 1: Create order
            when (val orderResult = orderRepository.createOrder(shippingAddress, billingAddress)) {
                is NetworkResult.Success -> {
                    val order = orderResult.data
                    // Save paymentId from order response for callback verification
                    preferenceManager.saveCurrentPaymentId(order.paymentId.toInt())
                    createPaymentForOrder(order.id.toInt())
                }
                is NetworkResult.Error -> {
                    _paymentUrlState.value = UiState.Error(
                        orderResult.message ?: "Không thể tạo đơn hàng", orderResult.code
                    )
                }
                is NetworkResult.Exception -> {
                    _paymentUrlState.value = UiState.Error(
                        orderResult.e.message ?: "Lỗi kết nối khi tạo đơn hàng"
                    )
                }
            }
        }
    }

    private suspend fun createPaymentForOrder(orderId: Int) {
        when (val result = paymentRepository.createPayment(orderId)) {
            is NetworkResult.Success -> {
                _paymentUrlState.value = UiState.Success(result.data.paymentUrl)
            }
            is NetworkResult.Error -> {
                _paymentUrlState.value = UiState.Error(
                    result.message ?: "Không thể tạo thanh toán VNPay", result.code
                )
            }
            is NetworkResult.Exception -> {
                _paymentUrlState.value = UiState.Error(
                    result.e.message ?: "Lỗi kết nối"
                )
            }
        }
    }

    override fun handleError(throwable: Throwable) {
        _paymentUrlState.value = UiState.Error(throwable.message ?: "Unknown error")
    }
}


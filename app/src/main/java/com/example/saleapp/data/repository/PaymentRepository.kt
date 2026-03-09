package com.example.saleapp.data.repository

import com.example.saleapp.core.network.ApiService
import com.example.saleapp.core.network.NetworkResult
import com.example.saleapp.data.model.request.CreatePaymentRequest
import com.example.saleapp.data.model.response.CreatePaymentResponse
import com.example.saleapp.data.model.response.PaymentStatusResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val apiService: ApiService
) {

    /**
     * Create VNPay payment URL for the given order.
     */
    suspend fun createPayment(orderId: Int): NetworkResult<CreatePaymentResponse> {
        return try {
            val response = apiService.createMobilePayment(CreatePaymentRequest(orderId = orderId))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    NetworkResult.Success(body)
                } else {
                    NetworkResult.Error(response.code(), "Empty response from server")
                }
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    /**
     * Get payment status from backend — source of truth after VNPay redirect.
     */
    suspend fun getPaymentStatus(paymentId: Int): NetworkResult<PaymentStatusResponse> {
        return try {
            val response = apiService.getPaymentStatus(paymentId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    NetworkResult.Success(body)
                } else {
                    NetworkResult.Error(response.code(), "Empty response from server")
                }
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }
}


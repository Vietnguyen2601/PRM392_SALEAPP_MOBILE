package com.example.saleapp.data.repository

import com.example.saleapp.core.network.ApiService
import com.example.saleapp.core.network.NetworkResult
import com.example.saleapp.data.model.request.CreateOrderRequest
import com.example.saleapp.data.model.response.OrderResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getOrders(): NetworkResult<List<OrderResponse>> {
        return try {
            val response = apiService.getOrders()
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    NetworkResult.Success(data)
                } else {
                    NetworkResult.Error(response.code(), body?.message ?: "Failed to fetch orders")
                }
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getOrderById(id: Long): NetworkResult<OrderResponse> {
        return try {
            val response = apiService.getOrderById(id)
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    NetworkResult.Success(data)
                } else {
                    NetworkResult.Error(response.code(), body?.message ?: "Order not found")
                }
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun createOrder(
        shippingAddress: String,
        paymentMethod: String,
        note: String? = null
    ): NetworkResult<OrderResponse> {
        return try {
            val response = apiService.createOrder(CreateOrderRequest(shippingAddress, paymentMethod, note))
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    NetworkResult.Success(data)
                } else {
                    NetworkResult.Error(response.code(), body?.message ?: "Failed to create order")
                }
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }
}


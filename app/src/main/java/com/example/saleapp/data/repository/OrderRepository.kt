package com.example.saleapp.data.repository

import android.util.Log
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
        billingAddress: String,
        shippingFee: Double = 0.0,
        discountAmount: Double = 0.0
    ): NetworkResult<OrderResponse> {
        return try {
            val request = CreateOrderRequest(shippingAddress, billingAddress, shippingFee, discountAmount)
            Log.d("OrderRepository", "createOrder request: $request")
            val response = apiService.createOrder(request)
            Log.d("OrderRepository", "createOrder response code: ${response.code()}")
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    NetworkResult.Success(body)
                } else {
                    Log.e("OrderRepository", "createOrder body error: null")
                    NetworkResult.Error(response.code(), "Failed to create order")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("OrderRepository", "createOrder error ${response.code()}: $errorBody")
                NetworkResult.Error(response.code(), errorBody ?: response.message())
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "createOrder exception: ${e.message}", e)
            NetworkResult.Exception(e)
        }
    }
}


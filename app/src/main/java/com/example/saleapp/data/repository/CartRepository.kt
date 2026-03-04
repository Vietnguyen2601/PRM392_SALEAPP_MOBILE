package com.example.saleapp.data.repository

import com.example.saleapp.core.network.ApiService
import com.example.saleapp.core.network.NetworkResult
import com.example.saleapp.data.model.request.AddToCartRequest
import com.example.saleapp.data.model.response.CartResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getCart(): NetworkResult<CartResponse> {
        return try {
            val response = apiService.getCart()
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    NetworkResult.Success(data)
                } else {
                    NetworkResult.Error(response.code(), body?.message ?: "Failed to fetch cart")
                }
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun addToCart(productId: Long, quantity: Int): NetworkResult<CartResponse> {
        return try {
            val response = apiService.addToCart(AddToCartRequest(productId, quantity))
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    NetworkResult.Success(data)
                } else {
                    NetworkResult.Error(response.code(), body?.message ?: "Failed to add to cart")
                }
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun removeFromCart(itemId: Long): NetworkResult<CartResponse> {
        return try {
            val response = apiService.removeFromCart(itemId)
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    NetworkResult.Success(data)
                } else {
                    NetworkResult.Error(response.code(), body?.message ?: "Failed to remove item")
                }
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun clearCart(): NetworkResult<Unit> {
        return try {
            val response = apiService.clearCart()
            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }
}


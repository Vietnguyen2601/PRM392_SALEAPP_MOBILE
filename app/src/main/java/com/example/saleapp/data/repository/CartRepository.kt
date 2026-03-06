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
                val data = response.body()
                if (data != null) {
                    NetworkResult.Success(data)
                } else {
                    NetworkResult.Error(response.code(), "Cart data is null")
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
            android.util.Log.d("CartRepository", "Adding to cart: productId=$productId, quantity=$quantity")
            val response = apiService.addToCart(AddToCartRequest(productId, quantity))
            android.util.Log.d("CartRepository", "Response code: ${response.code()}")
            android.util.Log.d("CartRepository", "Response isSuccessful: ${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val data = response.body()
                android.util.Log.d("CartRepository", "Response body: $data")
                
                if (data != null) {
                    android.util.Log.d("CartRepository", "Success! Total items: ${data.getTotalItems()}")
                    NetworkResult.Success(data)
                } else {
                    android.util.Log.e("CartRepository", "Cart data is null")
                    NetworkResult.Error(response.code(), "Cart data is null")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("CartRepository", "Error response: ${response.code()} - ${response.message()} - $errorBody")
                NetworkResult.Error(response.code(), response.message() + (errorBody?.let { " - $it" } ?: ""))
            }
        } catch (e: Exception) {
            android.util.Log.e("CartRepository", "Exception: ${e.message}", e)
            NetworkResult.Exception(e)
        }
    }

    suspend fun removeFromCart(itemId: Long): NetworkResult<CartResponse> {
        return try {
            val response = apiService.removeFromCart(itemId)
            if (response.isSuccessful) {
                val data = response.body()
                if (data != null) {
                    NetworkResult.Success(data)
                } else {
                    NetworkResult.Error(response.code(), "Failed to remove item")
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


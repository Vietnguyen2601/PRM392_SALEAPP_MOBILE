package com.example.saleapp.data.repository

import com.example.saleapp.core.network.ApiService
import com.example.saleapp.core.network.NetworkResult
import com.example.saleapp.data.model.response.ProductResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getAllProducts(): NetworkResult<List<ProductResponse>> {
        return try {
            val response = apiService.getAllProducts()
            if (response.isSuccessful) {
                val data = response.body()
                if (data != null) {
                    NetworkResult.Success(data)
                } else {
                    NetworkResult.Error(response.code(), "No products found")
                }
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getProducts(
        page: Int = 0,
        size: Int = 20,
        keyword: String? = null,
        categoryId: Long? = null,
        sort: String? = null
    ): NetworkResult<List<ProductResponse>> {
        return try {
            val response = apiService.getProducts(page, size, keyword, categoryId, sort)
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    NetworkResult.Success(data)
                } else {
                    NetworkResult.Error(response.code(), body?.message ?: "Failed to fetch products")
                }
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getProductById(id: Long): NetworkResult<ProductResponse> {
        return try {
            val response = apiService.getProductById(id)
            if (response.isSuccessful) {
                val data = response.body()
                if (data != null) {
                    NetworkResult.Success(data)
                } else {
                    NetworkResult.Error(response.code(), "Product not found")
                }
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }
}

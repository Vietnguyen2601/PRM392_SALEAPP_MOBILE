package com.example.saleapp.data.repository

import com.example.saleapp.core.network.ApiService
import com.example.saleapp.core.network.NetworkResult
import com.example.saleapp.data.model.StoreDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getStores(): NetworkResult<List<StoreDto>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = apiService.getStores()
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    NetworkResult.Success(data)
                } else {
                    NetworkResult.Error(response.code(), body?.message ?: "Failed to fetch stores")
                }
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getStoreDetail(id: Int): NetworkResult<StoreDto> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = apiService.getStoreDetail(id)
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    NetworkResult.Success(data)
                } else {
                    NetworkResult.Error(response.code(), body?.message ?: "Store not found")
                }
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun searchStores(
        latitude: Double,
        longitude: Double,
        radius: Int = 5
    ): NetworkResult<List<StoreDto>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = apiService.searchNearbyStores(latitude, longitude, radius)
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    NetworkResult.Success(data)
                } else {
                    NetworkResult.Error(response.code(), body?.message ?: "No stores found")
                }
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }
}


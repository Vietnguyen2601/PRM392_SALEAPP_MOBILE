package com.example.saleapp.data.repository

import com.example.saleapp.core.network.ApiService
import com.example.saleapp.core.network.NetworkResult
import com.example.saleapp.core.utils.PreferenceManager
import com.example.saleapp.data.model.request.LoginRequest
import com.example.saleapp.data.model.request.RegisterRequest
import com.example.saleapp.data.model.response.UserResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager
) {

    suspend fun login(email: String, password: String): NetworkResult<UserResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()
                val user = body?.data
                if (body?.success == true && user != null) {
                    user.token?.let { preferenceManager.saveAuthToken(it) }
                    preferenceManager.saveUserId(user.id.toString())
                    preferenceManager.saveUserEmail(user.email)
                    preferenceManager.setLoggedIn(true)
                    NetworkResult.Success(user)
                } else {
                    NetworkResult.Error(response.code(), body?.message ?: "Login failed")
                }
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun register(name: String, email: String, password: String, phone: String?): NetworkResult<UserResponse> {
        return try {
            val response = apiService.register(RegisterRequest(name, email, password, phone))
            if (response.isSuccessful) {
                val body = response.body()
                val user = body?.data
                if (body?.success == true && user != null) {
                    NetworkResult.Success(user)
                } else {
                    NetworkResult.Error(response.code(), body?.message ?: "Registration failed")
                }
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun logout(): NetworkResult<Unit> {
        return try {
            val response = apiService.logout()
            preferenceManager.clearAll()
            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            preferenceManager.clearAll()
            NetworkResult.Exception(e)
        }
    }

    fun isLoggedIn() = preferenceManager.isLoggedIn()
}


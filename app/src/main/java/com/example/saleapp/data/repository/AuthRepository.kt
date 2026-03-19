package com.example.saleapp.data.repository

import android.util.Base64
import com.example.saleapp.core.network.ApiService
import com.example.saleapp.core.network.NetworkResult
import com.example.saleapp.core.utils.PreferenceManager
import com.example.saleapp.data.model.request.LoginRequest
import com.example.saleapp.data.model.request.RegisterRequest
import com.example.saleapp.data.model.response.LoginResponse
import com.example.saleapp.data.model.response.UserResponse
import com.google.gson.Gson
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager
) {

    suspend fun login(username: String, password: String): NetworkResult<UserResponse> {
        return try {
            val response = apiService.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                val body = response.body()
                val user = body?.user
                if (body?.success == true && user != null) {
                    body.token?.let {
                        preferenceManager.saveAuthToken(it)
                        extractRoleFromToken(it)?.let { role -> preferenceManager.saveUserRole(role) }
                    }
                    preferenceManager.saveUserId(user.userId.toString())
                    preferenceManager.saveUserEmail(user.email)
                    user.role?.let { preferenceManager.saveUserRole(it) }
                    preferenceManager.setLoggedIn(true)
                    NetworkResult.Success(user)
                } else {
                    NetworkResult.Error(response.code(), body?.message ?: "Login failed")
                }
            } else {
                // Parse the error body to get the real message from the API (e.g. "Invalid username or password")
                val errorMessage = try {
                    val errorJson = response.errorBody()?.string()
                    val errorBody = Gson().fromJson(errorJson, LoginResponse::class.java)
                    errorBody?.message?.takeIf { it.isNotBlank() }
                } catch (e: Exception) {
                    null
                } ?: response.message()
                NetworkResult.Error(response.code(), errorMessage)
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun register(username: String, email: String, password: String, phoneNumber: String?, address: String?): NetworkResult<UserResponse> {
        return try {
            val response = apiService.register(RegisterRequest(username, password, email, phoneNumber, address))
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

    suspend fun getCurrentUser(): NetworkResult<UserResponse> {
        return try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful) {
                val body = response.body()
                val user = body?.data
                if (body?.success == true && user != null) {
                    NetworkResult.Success(user)
                } else {
                    NetworkResult.Error(response.code(), body?.message ?: "Failed to get user info")
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
            // Even if backend returns 404/401, clear local session and treat as logout success
            runCatching { apiService.logout() }
            preferenceManager.clearAll()
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            preferenceManager.clearAll()
            NetworkResult.Success(Unit)
        }
    }

    fun isLoggedIn() = preferenceManager.isLoggedIn()

    // Decode JWT payload to extract role claim if backend omits it from user object
    private fun extractRoleFromToken(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payloadJson = String(
                Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
            )
            val json = JSONObject(payloadJson)
            val msRoleKey = "http://schemas.microsoft.com/ws/2008/06/identity/claims/role"
            json.optString(msRoleKey)?.takeIf { it.isNotBlank() }
                ?: json.optString("role").takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }
}

package com.example.saleapp.core.network

import com.example.saleapp.data.model.request.AddToCartRequest
import com.example.saleapp.data.model.request.CreateOrderRequest
import com.example.saleapp.data.model.request.LoginRequest
import com.example.saleapp.data.model.request.RegisterRequest
import com.example.saleapp.data.model.response.BaseResponse
import com.example.saleapp.data.model.response.CartResponse
import com.example.saleapp.data.model.response.LoginResponse
import com.example.saleapp.data.model.response.OrderResponse
import com.example.saleapp.data.model.response.ProductResponse
import com.example.saleapp.data.model.response.RegisterResponse
import com.example.saleapp.data.model.response.UserResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("Auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/Auth/sign-up")
    suspend fun register(@Body request: RegisterRequest): Response<BaseResponse<UserResponse>>

    @GET("api/Auth/me")
    suspend fun getCurrentUser(): Response<BaseResponse<UserResponse>>

    @POST("auth/logout")
    suspend fun logout(): Response<BaseResponse<Unit>>

    // Products
    @GET("Products/getall")
    suspend fun getAllProducts(): Response<List<ProductResponse>>

    @GET("products")
    suspend fun getProducts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("keyword") keyword: String? = null,
        @Query("categoryId") categoryId: Long? = null,
        @Query("sort") sort: String? = null
    ): Response<BaseResponse<List<ProductResponse>>>

    @GET("Products/{id}")
    suspend fun getProductById(@Path("id") id: Long): Response<ProductResponse>

    // Cart
    @GET("Cart")
    suspend fun getCart(): Response<CartResponse>

    @POST("Cart/items")
    suspend fun addToCart(@Body request: AddToCartRequest): Response<CartResponse>

    @DELETE("Cart/items/{itemId}")
    suspend fun removeFromCart(@Path("itemId") itemId: Long): Response<CartResponse>

    @DELETE("Cart/clear")
    suspend fun clearCart(): Response<Unit>

    // Orders
    @GET("orders")
    suspend fun getOrders(): Response<BaseResponse<List<OrderResponse>>>

    @GET("orders/{id}")
    suspend fun getOrderById(@Path("id") id: Long): Response<BaseResponse<OrderResponse>>

    @POST("orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<BaseResponse<OrderResponse>>

    // User Profile
    @GET("users/me")
    suspend fun getUserProfile(): Response<BaseResponse<UserResponse>>
}


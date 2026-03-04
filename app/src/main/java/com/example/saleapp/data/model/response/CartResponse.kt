package com.example.saleapp.data.model.response

import com.google.gson.annotations.SerializedName

data class CartResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("items") val items: List<CartItemResponse>,
    @SerializedName("totalPrice") val totalPrice: Double,
    @SerializedName("totalItems") val totalItems: Int
)

data class CartItemResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("product") val product: ProductResponse,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("subtotal") val subtotal: Double
)


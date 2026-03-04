package com.example.saleapp.data.model.response

import com.google.gson.annotations.SerializedName

data class OrderResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("orderCode") val orderCode: String,
    @SerializedName("status") val status: String,
    @SerializedName("items") val items: List<OrderItemResponse>,
    @SerializedName("totalPrice") val totalPrice: Double,
    @SerializedName("shippingAddress") val shippingAddress: String,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("note") val note: String?,
    @SerializedName("createdAt") val createdAt: String
)

data class OrderItemResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("product") val product: ProductResponse,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("price") val price: Double,
    @SerializedName("subtotal") val subtotal: Double
)


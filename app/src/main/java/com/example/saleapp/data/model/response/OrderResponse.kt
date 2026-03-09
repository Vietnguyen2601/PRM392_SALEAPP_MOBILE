package com.example.saleapp.data.model.response

import com.google.gson.annotations.SerializedName

data class OrderResponse(
    @SerializedName("orderId") val id: Long,
    @SerializedName("orderStatus") val status: String,
    @SerializedName("paymentStatus") val paymentStatus: String,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("paymentId") val paymentId: Long,
    @SerializedName("items") val items: List<OrderItemResponse>,
    @SerializedName("subtotal") val subtotal: Double,
    @SerializedName("shippingFee") val shippingFee: Double,
    @SerializedName("discountAmount") val discountAmount: Double,
    @SerializedName("totalAmount") val totalAmount: Double,
    @SerializedName("shippingAddress") val shippingAddress: String,
    @SerializedName("billingAddress") val billingAddress: String,
    @SerializedName("customerName") val customerName: String,
    @SerializedName("customerEmail") val customerEmail: String,
    @SerializedName("totalItems") val totalItems: Int,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class OrderItemResponse(
    @SerializedName("orderItemId") val id: Long,
    @SerializedName("productId") val productId: Long,
    @SerializedName("productNameSnapshot") val productName: String,
    @SerializedName("unitPriceSnapshot") val unitPrice: Double,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("lineTotal") val lineTotal: Double
)


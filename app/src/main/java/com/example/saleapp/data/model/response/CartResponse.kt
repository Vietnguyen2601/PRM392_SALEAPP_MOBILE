package com.example.saleapp.data.model.response

import com.google.gson.annotations.SerializedName

data class CartResponse(
    @SerializedName("cartId") val cartId: Long? = null,
    @SerializedName("userId") val userId: Long? = null,
    @SerializedName("items") val items: List<CartItemResponse>? = null,
    @SerializedName("totalAmount") val totalAmount: Double? = null,
    @SerializedName("totalItems") val totalItems: Int? = null
) {
    // Helper functions to calculate if not provided by backend
    fun getTotalAmount(): Double = totalAmount ?: items?.sumOf { it.getDisplaySubtotal() } ?: 0.0
    fun getTotalItems(): Int = totalItems ?: items?.sumOf { it.quantity ?: 0 } ?: 0
}

data class CartItemResponse(
    @SerializedName("cartItemId") val cartItemId: Long? = null,
    @SerializedName("productId") val productId: Long? = null,
    @SerializedName("product") val product: ProductResponse? = null,
    @SerializedName("productName") val productName: String? = null,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("quantity") val quantity: Int? = null,
    @SerializedName("unitPrice") val unitPrice: Double? = null,
    @SerializedName("subtotal") val subtotal: Double? = null
) {
    // Helper functions to get values from either flat fields or nested product
    fun getDisplayProductId(): Long = productId ?: product?.getIdValue() ?: 0L
    fun getDisplayName(): String = productName ?: product?.getNameValue() ?: "Unknown"
    fun getDisplayImageUrl(): String = imageUrl ?: product?.imageUrl ?: ""
    fun getDisplayPrice(): Double = unitPrice ?: product?.getPriceValue() ?: 0.0
    fun getDisplaySubtotal(): Double = subtotal ?: ((quantity ?: 0) * getDisplayPrice())
}


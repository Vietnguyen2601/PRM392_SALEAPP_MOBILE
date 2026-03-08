package com.example.saleapp.data.model.response

import com.google.gson.annotations.SerializedName

data class ProductResponse(
    @SerializedName("productId") val productId: Long? = null,
    @SerializedName("id") val id: Long? = null,
    @SerializedName("productName") val productName: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("description") val description: String?,
    @SerializedName("currentPrice") val currentPrice: Double? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("originalPrice") val originalPrice: Double?,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("images") val images: List<String>?,
    @SerializedName("categoryId") val categoryId: Long?,
    @SerializedName("categoryName") val categoryName: String?,
    @SerializedName("stock") val stock: Int? = null,
    @SerializedName("rating") val rating: Float?,
    @SerializedName("averageRating") val averageRating: Float? = null,
    @SerializedName("reviewCount") val reviewCount: Int?,
    @SerializedName("totalReviews") val totalReviews: Int? = null,
    @SerializedName("totalSold") val totalSold: Int? = null,
    @SerializedName("technicalSpecifications") val technicalSpecifications: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("feedbacks") val feedbacks: List<Any>? = null
) {
    // Helper function to get the correct ID
    fun getIdValue(): Long = productId ?: id ?: 0L

    // Helper function to get the correct name
    fun getNameValue(): String = productName ?: name ?: "Unknown"

    // Helper function to get the correct price
    fun getPriceValue(): Double = currentPrice ?: price ?: 0.0

    // Helper function to get the correct rating
    fun getRatingValue(): Float = averageRating ?: rating ?: 0f
}


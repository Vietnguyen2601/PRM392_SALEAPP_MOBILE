package com.example.saleapp.data.model.response

import com.google.gson.annotations.SerializedName

data class ProductResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("price") val price: Double,
    @SerializedName("originalPrice") val originalPrice: Double?,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("images") val images: List<String>?,
    @SerializedName("categoryId") val categoryId: Long?,
    @SerializedName("categoryName") val categoryName: String?,
    @SerializedName("stock") val stock: Int,
    @SerializedName("rating") val rating: Float?,
    @SerializedName("reviewCount") val reviewCount: Int?
)


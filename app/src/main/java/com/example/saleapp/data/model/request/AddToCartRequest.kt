package com.example.saleapp.data.model.request

import com.google.gson.annotations.SerializedName

data class AddToCartRequest(
    @SerializedName("productId") val productId: Long,
    @SerializedName("quantity") val quantity: Int
)


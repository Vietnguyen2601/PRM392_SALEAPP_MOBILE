package com.example.saleapp.data.model.request

import com.google.gson.annotations.SerializedName

data class CreateOrderRequest(
    @SerializedName("shippingAddress") val shippingAddress: String,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("note") val note: String? = null
)


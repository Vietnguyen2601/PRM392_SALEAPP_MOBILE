package com.example.saleapp.data.model.request

import com.google.gson.annotations.SerializedName

data class CreateOrderRequest(
    @SerializedName("shippingAddress") val shippingAddress: String,
    @SerializedName("billingAddress") val billingAddress: String,
    @SerializedName("shippingFee") val shippingFee: Double = 0.0,
    @SerializedName("discountAmount") val discountAmount: Double = 0.0
)


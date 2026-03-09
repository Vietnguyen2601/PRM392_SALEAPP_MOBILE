package com.example.saleapp.data.model.request

import com.google.gson.annotations.SerializedName

data class CreatePaymentRequest(
    @SerializedName("orderId")
    val orderId: Int
)


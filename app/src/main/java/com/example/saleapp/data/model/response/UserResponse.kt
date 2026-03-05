package com.example.saleapp.data.model.response

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("userId") val userId: Long,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("phoneNumber") val phoneNumber: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("isActive") val isActive: Boolean
)


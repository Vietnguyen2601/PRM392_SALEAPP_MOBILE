package com.example.saleapp.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class StoreDto(
    @SerializedName("locationId")
    val locationId: Int,

    @SerializedName("address")
    val address: String,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("distance")
    val distance: Double? = null
) : Serializable


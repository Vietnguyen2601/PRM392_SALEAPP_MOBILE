package com.example.saleapp.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * Lightweight order summary returned by GET /Orders/my (paginated list).
 * Fields match exactly what the backend returns per item.
 */
data class MyOrderSummaryResponse(
    @SerializedName("orderId")       val orderId: Long,
    @SerializedName("userId")        val userId: Long,
    @SerializedName("customerName")  val customerName: String,
    @SerializedName("orderStatus")   val orderStatus: String,
    @SerializedName("shippingAddress") val shippingAddress: String,
    @SerializedName("subtotal")      val subtotal: Double,
    @SerializedName("shippingFee")   val shippingFee: Double,
    @SerializedName("discountAmount") val discountAmount: Double,
    @SerializedName("totalAmount")   val totalAmount: Double,
    @SerializedName("totalItems")    val totalItems: Int,
    @SerializedName("createdAt")     val createdAt: String
)

/**
 * Paginated wrapper for GET /Orders/my.
 * Backend shape:
 * { "items": [...], "totalCount": 2, "page": 1, "pageSize": 10, "totalPages": 1 }
 */
data class PagedOrderResponse(
    @SerializedName("items")      val items: List<MyOrderSummaryResponse>,
    @SerializedName("totalCount") val totalCount: Int,
    @SerializedName("page")       val page: Int,
    @SerializedName("pageSize")   val pageSize: Int,
    @SerializedName("totalPages") val totalPages: Int
)

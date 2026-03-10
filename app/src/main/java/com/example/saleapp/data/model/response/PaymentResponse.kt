package com.example.saleapp.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * Response from backend when creating a VNPay payment URL.
 */
data class CreatePaymentResponse(
    @SerializedName("paymentUrl")
    val paymentUrl: String
)

/**
 * Payment status response for verification after VNPay redirect.
 * Status values: Pending, Paid, Failed, Expired
 */
data class PaymentStatusResponse(
    @SerializedName("paymentId")
    val paymentId: Int,

    @SerializedName("orderId")
    val orderId: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("isPaid")
    val isPaid: Boolean,

    @SerializedName("transactionId")
    val transactionId: String?,

    @SerializedName("paidAt")
    val paidAt: String?,

    @SerializedName("message")
    val message: String,

    @SerializedName("canRetry")
    val canRetry: Boolean
)


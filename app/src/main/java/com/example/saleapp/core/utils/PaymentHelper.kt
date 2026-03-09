package com.example.saleapp.core.utils

import android.net.Uri

object PaymentHelper {

    /**
     * Extract payment ID from VNPay payment URL.
     *
     * vnp_TxnRef format: "{paymentId}{yyyyMMddHHmmss}"
     * Example: "12320260309001234" → paymentId = 123 (first N digits, last 14 are timestamp)
     *
     * NOTE: If the backend returns paymentId directly in CreatePaymentResponse,
     * prefer using that instead of parsing the URL.
     */
    fun extractPaymentId(paymentUrl: String): Int {
        return try {
            val uri = Uri.parse(paymentUrl)
            val txnRef = uri.getQueryParameter("vnp_TxnRef") ?: return 0
            // Remove last 14 characters (timestamp: yyyyMMddHHmmss)
            val paymentIdStr = if (txnRef.length > 14) {
                txnRef.substring(0, txnRef.length - 14)
            } else {
                txnRef
            }
            paymentIdStr.toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Extract TmnCode from VNPay payment URL (required by SDK).
     */
    fun extractTmnCode(paymentUrl: String): String {
        return try {
            val uri = Uri.parse(paymentUrl)
            uri.getQueryParameter("vnp_TmnCode") ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}


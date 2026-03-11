package com.example.saleapp.core.utils

object Constants {
    // SharedPreferences Keys
    const val PREF_NAME = "SalesAppPreferences"
    const val KEY_AUTH_TOKEN = "auth_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_EMAIL = "user_email"
    const val KEY_IS_LOGGED_IN = "is_logged_in"
    const val KEY_USER_ROLE = "user_role"

    // API Request Keys
    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val CONTENT_TYPE_JSON = "application/json"

    // Intent Extras
    const val EXTRA_PRODUCT_ID = "product_id"
    const val EXTRA_ORDER_ID = "order_id"

    // Network
    const val NETWORK_TIMEOUT = 30L // seconds

    // Pagination
    const val PAGE_SIZE = 20

    // VNPay
    const val VNPAY_RETURN_URL = "saleapp://payment/callback"

    // Sort Options
    const val SORT_PRICE_LOW_TO_HIGH = "price_asc"
    const val SORT_PRICE_HIGH_TO_LOW = "price_desc"
    const val SORT_NAME_A_TO_Z = "name_asc"
    const val SORT_NEWEST = "newest"
}
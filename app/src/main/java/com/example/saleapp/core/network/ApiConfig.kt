package com.example.saleapp.core.network

/**
 * Central API configuration.
 *
 * HOW TO SWITCH BETWEEN EMULATOR AND REAL DEVICE:
 *
 * 1. Emulator (default):
 *    BASE_URL = "http://10.0.2.2:5000/api/"
 *    (10.0.2.2 is the loopback alias for your host machine on Android emulator)
 *
 * 2. Real device (WiFi):
 *    - Find your computer's WiFi IP:
 *        Windows: run `ipconfig` → look for "IPv4 Address" under your WiFi adapter
 *        Example: 192.168.1.105
 *    - Set BASE_URL = "http://192.168.1.105:5000/api/"
 *    - Make sure both your computer and phone are on the SAME WiFi network
 *    - Make sure the backend is listening on 0.0.0.0 (not just localhost)
 *
 * 3. Production:
 *    BASE_URL = "https://your-production-domain.com/api/"
 *
 * IMPORTANT: Change DEVICE_TYPE below to switch. Do NOT push REAL_DEVICE config to git.
 */
object ApiConfig {

    private enum class DeviceType { EMULATOR, REAL_DEVICE, PRODUCTION }

    // ←── CHANGE THIS to switch target
    private val DEVICE_TYPE = DeviceType.EMULATOR

    // ←── Set your computer's WiFi IP here when testing on a real device
    private const val WIFI_IP = "192.168.1.105"

    val BASE_URL: String get() = when (DEVICE_TYPE) {
        DeviceType.EMULATOR    -> "http://10.0.2.2:5000/api/"
        DeviceType.REAL_DEVICE -> "http://$WIFI_IP:5000/api/"
        DeviceType.PRODUCTION  -> "https://your-production-domain.com/api/"
    }

    // Deep link returned to VNPay so it redirects back to the app after payment
    const val VNPAY_RETURN_URL = "saleapp://payment/callback"
}

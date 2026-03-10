package com.example.saleapp.ui.checkout

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.lifecycleScope
import com.example.saleapp.core.base.BaseActivity
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.core.utils.showToast
import com.example.saleapp.databinding.ActivityCheckoutBinding
import com.example.saleapp.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// TODO: Uncomment when merchant-1.0.25.aar is placed in app/libs/ and project is synced
// import com.example.saleapp.core.utils.PaymentHelper
// import vn.vnpay.authentication.VNP_AuthenticationActivity
// import vn.vnpay.authentication.VNP_SdkCompletedCallback

@AndroidEntryPoint
class CheckoutActivity : BaseActivity<ActivityCheckoutBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityCheckoutBinding =
        ActivityCheckoutBinding::inflate

    private val viewModel: CheckoutViewModel by viewModels()

    override fun setupViews() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnPay.setOnClickListener {
            val shippingAddress = binding.etShippingAddress.text?.toString()?.trim() ?: ""
            val billingAddress = binding.etBillingAddress.text?.toString()?.trim() ?: ""

            var hasError = false
            if (shippingAddress.isEmpty()) {
                binding.tilShippingAddress.error = "Vui lòng nhập địa chỉ giao hàng"
                hasError = true
            } else {
                binding.tilShippingAddress.error = null
            }
            if (billingAddress.isEmpty()) {
                binding.tilBillingAddress.error = "Vui lòng nhập địa chỉ thanh toán"
                hasError = true
            } else {
                binding.tilBillingAddress.error = null
            }
            if (hasError) return@setOnClickListener

            if (!isNetworkAvailable()) {
                showToast("Không có kết nối mạng. Vui lòng kiểm tra WiFi hoặc dữ liệu di động.")
                return@setOnClickListener
            }

            viewModel.checkout(shippingAddress = shippingAddress, billingAddress = billingAddress)
        }
    }

    override fun observeData() {
        // Observe payment URL → open Chrome Custom Tabs
        lifecycleScope.launch {
            viewModel.paymentUrlState.collectLatest { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnPay.isEnabled = false
                    }
                    is UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnPay.isEnabled = true
                        openPaymentUrl(state.data)
                    }
                    is UiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnPay.isEnabled = true
                        showToast(state.message)
                    }
                    is UiState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnPay.isEnabled = true
                    }
                }
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Open VNPay payment URL in Chrome Custom Tabs.
     * After payment, VNPay redirects to deep link: saleapp://payment/callback
     * which launches PaymentCallbackActivity to verify the result from the backend.
     *
     * TODO: Replace with openVnPaySDK() once merchant-1.0.25.aar is available.
     */
    private fun openPaymentUrl(paymentUrl: String) {
        Log.d(TAG, "=== PAYMENT URL ===")
        Log.d(TAG, paymentUrl)
        Log.d(TAG, "===================")

        val uri = Uri.parse(paymentUrl)
        Log.d(TAG, "Host: ${uri.host}, Scheme: ${uri.scheme}")
        uri.queryParameterNames.forEach { Log.d(TAG, "$it = ${uri.getQueryParameter(it)}") }

        val secureHash = uri.getQueryParameter("vnp_SecureHash")
        if (secureHash.isNullOrEmpty()) {
            Log.e(TAG, "MISSING vnp_SecureHash!")
            showToast("Payment URL không hợp lệ (thiếu SecureHash)")
            return
        }

        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.launchUrl(this, uri)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening Custom Tabs", e)
            showToast("Không thể mở trang thanh toán: ${e.message}")
        }
    }

    // -------------------------------------------------------------------------
    // TODO: VNPay SDK integration — uncomment after placing AAR in app/libs/
    // -------------------------------------------------------------------------
    //
    // private fun openVnPaySDK(paymentUrl: String) {
    //     try {
    //         val tmnCode = PaymentHelper.extractTmnCode(paymentUrl)
    //         VNP_AuthenticationActivity.setSdkCompletedCallback(object : VNP_SdkCompletedCallback {
    //             override fun sdkAction(action: String) {
    //                 Log.d(TAG, "SDK Callback: $action")
    //                 handleSdkCallback(action)
    //             }
    //         })
    //         val intent = Intent(this, VNP_AuthenticationActivity::class.java).apply {
    //             putExtra("url", paymentUrl)
    //             putExtra("tmn_code", tmnCode)
    //             putExtra("scheme", "")
    //             putExtra("is_sandbox", true)
    //         }
    //         startActivity(intent)
    //     } catch (e: Exception) {
    //         Log.e(TAG, "Error opening VNPay SDK", e)
    //         showToast("Không thể mở VNPay: ${e.message}")
    //     }
    // }
    //
    // private fun handleSdkCallback(action: String) {
    //     when (action) {
    //         "AppBackAction", "WebBackAction" -> runOnUiThread { showToast("Đã hủy thanh toán") }
    //         "CallMobileBankingApp" -> Log.d(TAG, "User opening mobile banking app")
    //         "SuccessBackAction", "FaildBackAction" -> runOnUiThread { viewModel.verifyPayment() }
    //         else -> Log.w(TAG, "Unknown SDK action: $action")
    //     }
    // }

    private fun navigateToMain(success: Boolean, orderId: Int = 0) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("payment_success", success)
            if (orderId > 0) putExtra("order_id", orderId)
        }
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "CheckoutActivity"
    }
}
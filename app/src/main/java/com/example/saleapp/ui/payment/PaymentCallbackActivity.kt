package com.example.saleapp.ui.payment

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.saleapp.core.base.BaseActivity
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.databinding.ActivityPaymentCallbackBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Deep Link handler for VNPay payment callback.
 * Launched when VNPay redirects to: saleapp://payment/callback
 *
 * Flow:
 * 1. VNPay redirects user to deep link
 * 2. This activity is launched
 * 3. Reads paymentId from PreferenceManager (saved by CheckoutActivity)
 * 4. Calls backend to verify payment status (source of truth)
 * 5. Navigates to main screen with success/failure message
 */
@AndroidEntryPoint
class PaymentCallbackActivity : BaseActivity<ActivityPaymentCallbackBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityPaymentCallbackBinding =
        ActivityPaymentCallbackBinding::inflate

    private val viewModel: PaymentCallbackViewModel by viewModels()

    // VNPay response code từ deep link URI — "00" nghĩa là VNPay xác nhận thành công
    private var vnpResponseCode: String = ""

    override fun setupViews() {
        // Prevent back-press while verifying
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Block back press during verification
            }
        })

        // Đọc tất cả params từ deep link: saleapp://payment/callback?vnp_ResponseCode=00&...
        // Sau đó forward nguyên vẹn lên backend Payment/vnpay/callback
        val vnpayParams = mutableMapOf<String, String>()
        intent.data?.let { uri ->
            uri.queryParameterNames.forEach { key ->
                uri.getQueryParameter(key)?.let { value -> vnpayParams[key] = value }
            }
            vnpResponseCode = vnpayParams["vnp_ResponseCode"] ?: ""
            val txnStatus = vnpayParams["vnp_TransactionStatus"] ?: ""
            Log.d(TAG, "Deep link params: ResponseCode=$vnpResponseCode TxnStatus=$txnStatus total=${vnpayParams.size}")
        }

        viewModel.verifyPayment(vnpayParams)
    }

    override fun observeData() {
        lifecycleScope.launch {
            viewModel.paymentStatusState.collectLatest { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.tvStatus.text = "Đang xác nhận thanh toán..."
                    }
                    is UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        val data = state.data
                        when {
                            // Backend xác nhận đã thanh toán
                            data.isPaid -> navigateToPaymentResult(
                                isPaid = true, orderId = data.orderId, message = ""
                            )
                            // Backend chưa nhận IPN nhưng VNPay đã xác nhận thành công (ResponseCode=00)
                            data.status == "Pending" && vnpResponseCode == "00" -> {
                                Log.d(TAG, "Backend Pending but VNPay ResponseCode=00, treating as success")
                                navigateToPaymentResult(
                                    isPaid = true, orderId = data.orderId, message = ""
                                )
                            }
                            // Thực sự thất bại
                            else -> navigateToPaymentResult(
                                isPaid = false, orderId = 0, message = data.message
                            )
                        }
                    }
                    is UiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        // Không kết nối được backend — dùng ResponseCode của VNPay làm fallback
                        val isPaid = vnpResponseCode == "00"
                        navigateToPaymentResult(
                            isPaid = isPaid,
                            orderId = 0,
                            message = if (isPaid) "" else state.message
                        )
                    }
                    is UiState.Idle -> Unit
                }
            }
        }
    }

    private fun navigateToPaymentResult(isPaid: Boolean, orderId: Int, message: String) {
        val intent = Intent(this, PaymentResultActivity::class.java).apply {
            putExtra(PaymentResultActivity.EXTRA_IS_PAID, isPaid)
            putExtra(PaymentResultActivity.EXTRA_ORDER_ID, orderId)
            putExtra(PaymentResultActivity.EXTRA_FAILURE_MESSAGE, message)
        }
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "PaymentCallbackActivity"
    }
}


package com.example.saleapp.ui.payment

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.saleapp.core.base.BaseActivity
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.core.utils.showToast
import com.example.saleapp.databinding.ActivityPaymentCallbackBinding
import com.example.saleapp.ui.main.MainActivity
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

    override fun setupViews() {
        // Prevent back-press while verifying
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Block back press during verification
            }
        })

        viewModel.verifyPayment()
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
                        val paymentStatus = state.data
                        if (paymentStatus.isPaid) {
                            binding.progressBar.visibility = View.GONE
                            binding.tvStatus.text = "Thanh toán thành công!"
                            navigateToMainWithSuccess(paymentStatus.orderId)
                        } else {
                            binding.progressBar.visibility = View.GONE
                            binding.tvStatus.text = paymentStatus.message
                            showToast(paymentStatus.message)
                            navigateToMainWithFailure(paymentStatus.message, paymentStatus.canRetry)
                        }
                    }
                    is UiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.tvStatus.text = state.message
                        showToast(state.message)
                        navigateToMain()
                    }
                    is UiState.Idle -> Unit
                }
            }
        }
    }

    private fun navigateToMainWithSuccess(orderId: Int) {
        showToast("🎉 Thanh toán thành công! Đơn hàng #$orderId đang được xử lý.")
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_PAYMENT_SUCCESS, true)
            putExtra(EXTRA_ORDER_ID, orderId)
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToMainWithFailure(message: String, canRetry: Boolean) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_PAYMENT_SUCCESS, false)
            putExtra(EXTRA_PAYMENT_MESSAGE, message)
            putExtra(EXTRA_CAN_RETRY, canRetry)
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_PAYMENT_SUCCESS = "payment_success"
        const val EXTRA_ORDER_ID = "order_id"
        const val EXTRA_PAYMENT_MESSAGE = "payment_message"
        const val EXTRA_CAN_RETRY = "can_retry"
    }
}


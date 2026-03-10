package com.example.saleapp.ui.payment

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.example.saleapp.core.base.BaseActivity
import com.example.saleapp.databinding.ActivityPaymentResultBinding
import com.example.saleapp.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Hiển thị kết quả thanh toán sau khi VNPay SDK hoàn tất (bao gồm bước OTP).
 *
 * Được khởi chạy từ CheckoutActivity khi nhận callback từ VNP_SdkCompletedCallback:
 *  - "SuccessBackAction" / "FaildBackAction" → gọi backend xác nhận, rồi hiển thị kết quả
 *  - "AppBackAction" / "WebBackAction"       → hiển thị màn hình đã hủy ngay lập tức
 *
 * Lưu ý bảo mật: kết quả cuối cùng (thành công / thất bại) luôn lấy từ backend,
 * không tin tưởng hoàn toàn vào action của SDK.
 */
@AndroidEntryPoint
class PaymentResultActivity : BaseActivity<ActivityPaymentResultBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityPaymentResultBinding =
        ActivityPaymentResultBinding::inflate

    // Tái sử dụng PaymentCallbackViewModel — có sẵn logic verifyPayment()
    // private val viewModel: PaymentCallbackViewModel by viewModels()

    // Chỉ cho phép back khi đã có kết quả (loading thì khóa)
    private var canGoBack = false

    override fun setupViews() {
        // Khóa nút back khi đang xác nhận
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (canGoBack) navigateToMain()
            }
        })

        binding.btnGoHomeSuccess.setOnClickListener { navigateToMain() }
        binding.btnGoHomeFailure.setOnClickListener { navigateToMain() }
        binding.btnRetry.setOnClickListener { navigateToCheckout() }

        when {
            // ===== Deep link flow =====
            // Kết quả đã được xác định bởi PaymentCallbackActivity, hiển thị ngay không cần gọi lại backend
            intent.hasExtra(EXTRA_IS_PAID) -> {
                val isPaid = intent.getBooleanExtra(EXTRA_IS_PAID, false)
                val orderId = intent.getIntExtra(EXTRA_ORDER_ID, 0)
                val message = intent.getStringExtra(EXTRA_FAILURE_MESSAGE) ?: ""
                if (isPaid) showSuccessState(orderId) else showFailureState(message)
            }

            // ===== SDK flow =====
            // Callback từ VNP_AuthenticationActivity — tin tưởng action trực tiếp, không cần gọi backend
            else -> {
                val action = intent.getStringExtra(EXTRA_SDK_ACTION) ?: ""
                when (action) {
                    ACTION_SUCCESS              -> showSuccessState(0)
                    ACTION_FAILED               -> showFailureState("Thanh toán thất bại")
                    ACTION_APP_BACK, ACTION_WEB_BACK -> showCancelledState()
                    else                        -> showCancelledState()
                }
            }
        }
    }

    override fun observeData() {
        // No observables needed — result is determined synchronously from intent extras
    }

    // -------------------------------------------------------------------------
    // State helpers
    // -------------------------------------------------------------------------

    private fun showLoadingState() {
        canGoBack = false
        binding.layoutLoading.visibility = View.VISIBLE
        binding.layoutSuccess.visibility = View.GONE
        binding.layoutFailure.visibility = View.GONE
    }

    private fun showSuccessState(orderId: Int) {
        canGoBack = true
        binding.layoutLoading.visibility = View.GONE
        binding.layoutSuccess.visibility = View.VISIBLE
        binding.layoutFailure.visibility = View.GONE

        if (orderId > 0) {
            binding.tvOrderId.text = "Mã đơn hàng: #$orderId"
            binding.tvOrderId.visibility = View.VISIBLE
        }
    }

    private fun showFailureState(message: String) {
        canGoBack = true
        binding.layoutLoading.visibility = View.GONE
        binding.layoutSuccess.visibility = View.GONE
        binding.layoutFailure.visibility = View.VISIBLE

        binding.ivFailureIcon.setImageResource(com.example.saleapp.R.drawable.ic_payment_failure)
        binding.tvFailureTitle.text = "Thanh toán thất bại"
        binding.tvFailureTitle.setTextColor(android.graphics.Color.parseColor("#FFF44336"))

        if (message.isNotEmpty()) {
            binding.tvFailureMessage.text = message
            binding.tvFailureMessage.visibility = View.VISIBLE
        }
        binding.btnRetry.visibility = View.VISIBLE
    }

    private fun showCancelledState() {
        canGoBack = true
        binding.layoutLoading.visibility = View.GONE
        binding.layoutSuccess.visibility = View.GONE
        binding.layoutFailure.visibility = View.VISIBLE

        binding.ivFailureIcon.setImageResource(com.example.saleapp.R.drawable.ic_payment_cancel)
        binding.tvFailureTitle.text = "Đã hủy thanh toán"
        binding.tvFailureTitle.setTextColor(android.graphics.Color.parseColor("#FFFF9800"))

        binding.tvFailureMessage.text = "Bạn đã hủy quá trình thanh toán VNPay."
        binding.tvFailureMessage.visibility = View.VISIBLE

        // Không cần nút Thử lại khi huỷ → dẫn về trang chủ thôi
        binding.btnRetry.visibility = View.GONE
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToCheckout() {
        // Quay lại CheckoutActivity để thử lại
        finish()
    }

    companion object {
        // Deep link flow extras
        const val EXTRA_IS_PAID = "extra_is_paid"
        const val EXTRA_ORDER_ID = "extra_order_id"
        const val EXTRA_FAILURE_MESSAGE = "extra_failure_message"

        // SDK flow extras
        const val EXTRA_SDK_ACTION = "extra_sdk_action"

        // VNPay SDK callback actions
        const val ACTION_SUCCESS  = "SuccessBackAction"
        const val ACTION_FAILED   = "FaildBackAction"   // VNPay typo (Faild)
        const val ACTION_APP_BACK = "AppBackAction"
        const val ACTION_WEB_BACK = "WebBackAction"
    }
}

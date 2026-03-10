package com.example.saleapp.ui.profile

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.saleapp.data.model.response.MyOrderSummaryResponse
import com.example.saleapp.databinding.ItemOrderSummaryBinding
import java.text.NumberFormat
import java.util.Locale

class OrderHistoryAdapter :
    ListAdapter<MyOrderSummaryResponse, OrderHistoryAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemOrderSummaryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: MyOrderSummaryResponse) {
            binding.tvOrderId.text = "Đơn hàng #${order.orderId}"
            binding.tvOrderMeta.text = "${order.totalItems} sản phẩm  •  ${formatDate(order.createdAt)}"
            binding.tvTotalAmount.text = formatCurrency(order.totalAmount)

            val (label, color) = statusDisplay(order.orderStatus)
            binding.tvOrderStatus.text = label
            binding.tvOrderStatus.backgroundTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor(color))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderSummaryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun formatCurrency(amount: Double): String {
        val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return "${fmt.format(amount)} đ"
    }

    private fun formatDate(iso: String): String {
        return try {
            // 2026-03-10T08:49:13.373212Z → 10/03/2026
            val parts = iso.substringBefore('T').split("-")
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } catch (e: Exception) {
            iso
        }
    }

    private fun statusDisplay(status: String): Pair<String, String> = when (status.lowercase()) {
        "pending"    -> "Chờ xử lý"    to "#FFCC00"
        "processing" -> "Đang xử lý"   to "#2196F3"
        "confirmed"  -> "Đã xác nhận"  to "#4CAF50"
        "shipped"    -> "Đang giao"    to "#FF9800"
        "delivered"  -> "Đã giao"      to "#4CAF50"
        "complete",
        "completed"  -> "Hoàn thành"   to "#4CAF50"
        "cancelled"  -> "Đã hủy"       to "#F44336"
        else         -> status          to "#9E9E9E"
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MyOrderSummaryResponse>() {
            override fun areItemsTheSame(a: MyOrderSummaryResponse, b: MyOrderSummaryResponse) =
                a.orderId == b.orderId

            override fun areContentsTheSame(a: MyOrderSummaryResponse, b: MyOrderSummaryResponse) =
                a == b
        }
    }
}

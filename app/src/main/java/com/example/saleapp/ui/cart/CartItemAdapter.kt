package com.example.saleapp.ui.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.saleapp.data.model.response.CartItemResponse
import com.example.saleapp.databinding.ItemCartBinding

class CartItemAdapter(
    private val onRemoveClick: (CartItemResponse) -> Unit
) : ListAdapter<CartItemResponse, CartItemAdapter.CartItemViewHolder>(CartItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartItemViewHolder(binding, onRemoveClick)
    }

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CartItemViewHolder(
        private val binding: ItemCartBinding,
        private val onRemoveClick: (CartItemResponse) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItemResponse) {
            binding.apply {
                // Load product image
                ivProductImage.load(item.getDisplayImageUrl()) {
                    crossfade(true)
                    placeholder(android.R.drawable.ic_menu_gallery)
                    error(android.R.drawable.ic_menu_gallery)
                }

                // Set product details
                tvProductName.text = item.getDisplayName()
                tvUnitPrice.text = "$${String.format("%.2f", item.getDisplayPrice())}"
                tvQuantity.text = "Quantity: ${item.quantity ?: 0}"
                tvSubtotal.text = "Subtotal: $${String.format("%.2f", item.getDisplaySubtotal())}"

                // Handle remove button click
                btnRemove.setOnClickListener {
                    onRemoveClick(item)
                }
            }
        }
    }

    private class CartItemDiffCallback : DiffUtil.ItemCallback<CartItemResponse>() {
        override fun areItemsTheSame(oldItem: CartItemResponse, newItem: CartItemResponse): Boolean {
            return oldItem.cartItemId == newItem.cartItemId
        }

        override fun areContentsTheSame(oldItem: CartItemResponse, newItem: CartItemResponse): Boolean {
            return oldItem == newItem
        }
    }
}

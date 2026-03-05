package com.example.saleapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.saleapp.data.model.response.ProductResponse
import com.example.saleapp.databinding.ItemProductBinding

class ProductAdapter(
    private val products: MutableList<ProductResponse> = mutableListOf(),
    private val onProductClick: (ProductResponse) -> Unit = {}
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductResponse) {
            binding.apply {
                // Load image using Coil
                ivProductImage.load(product.imageUrl) {
                    crossfade(true)
                }

                // Product name
                tvProductName.text = product.getNameValue()

                // Category
                tvCategory.text = product.categoryName ?: "Unknown Category"

                // Rating and reviews
                tvRating.text = "★ ${product.getRatingValue()}"
                tvReviews.text = "(${product.totalReviews ?: product.reviewCount ?: 0} reviews)"

                // Price
                tvPrice.text = "$${String.format("%.2f", product.getPriceValue())}"

                // Description
                tvDescription.text = product.description ?: "No description available"

                // Click listener
                root.setOnClickListener {
                    onProductClick(product)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<ProductResponse>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }
}


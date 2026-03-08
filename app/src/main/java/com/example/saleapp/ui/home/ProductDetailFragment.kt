package com.example.saleapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.saleapp.core.base.BaseFragment
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.core.utils.showToast
import com.example.saleapp.data.model.response.ProductResponse
import com.example.saleapp.databinding.FragmentProductDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductDetailFragment : BaseFragment<FragmentProductDetailBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentProductDetailBinding =
        FragmentProductDetailBinding::inflate

    private val viewModel: ProductDetailViewModel by viewModels()
    private val args: ProductDetailFragmentArgs by navArgs()
    private var currentProductId: Long = 0
    private var currentQuantity: Int = 1

    override fun setupViews() {
        val productId: Long = args.productId
        currentProductId = productId
        viewModel.loadProductDetails(productId)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        setupQuantitySelector()

        binding.btnAddToCart.setOnClickListener {
            android.util.Log.d("ProductDetailFragment", "Add to cart clicked, productId=$currentProductId, quantity=$currentQuantity")
            viewModel.addToCart(currentProductId, currentQuantity)
        }
    }

    private fun setupQuantitySelector() {
        binding.btnDecreaseQuantity.setOnClickListener {
            if (currentQuantity > 1) {
                currentQuantity--
                binding.tvQuantity.text = currentQuantity.toString()
            }
        }

        binding.btnIncreaseQuantity.setOnClickListener {
            if (currentQuantity < 99) { // Max 99 items
                currentQuantity++
                binding.tvQuantity.text = currentQuantity.toString()
            }
        }
    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.productState.collect { state ->
                when (state) {
                    is UiState.Loading -> showLoading(true)
                    is UiState.Success -> {
                        showLoading(false)
                        displayProduct(state.data)
                    }
                    is UiState.Error -> {
                        showLoading(false)
                        showToast(state.message)
                    }
                    else -> showLoading(false)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.addToCartState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.btnAddToCart.isEnabled = false
                        binding.btnAddToCart.text = "Adding..."
                    }
                    is UiState.Success -> {
                        binding.btnAddToCart.isEnabled = true
                        binding.btnAddToCart.text = "Add to Cart"
                        val totalItems = state.data.getTotalItems()
                        android.util.Log.d("ProductDetailFragment", "Added to cart successfully, total items: $totalItems")
                        showToast("Added $currentQuantity item(s) to cart! Total: $totalItems")
                        // Reset quantity to 1 after successful add
                        currentQuantity = 1
                        binding.tvQuantity.text = "1"
                    }
                    is UiState.Error -> {
                        binding.btnAddToCart.isEnabled = true
                        binding.btnAddToCart.text = "Add to Cart"
                        android.util.Log.e("ProductDetailFragment", "Failed to add to cart: ${state.message}")
                        showToast("Failed: ${state.message}")
                    }
                    else -> {
                        binding.btnAddToCart.isEnabled = true
                        binding.btnAddToCart.text = "Add to Cart"
                    }
                }
            }
        }
    }

    private fun displayProduct(product: ProductResponse) {
        binding.apply {
            // Image
            ivProductImage.load(product.imageUrl) {
                crossfade(true)
            }

            // Name
            tvProductName.text = product.getNameValue()

            // Category
            tvCategory.text = product.categoryName ?: "Unknown"

            // Rating and reviews
            tvRating.text = "★ ${product.getRatingValue()}"
            tvReviews.text = "(${product.totalReviews ?: 0} reviews)"

            // Price
            tvPrice.text = "$${String.format("%.2f", product.getPriceValue())}"

            // Description
            tvDescription.text = product.description ?: "No description available"

            // Technical specifications
            if (!product.technicalSpecifications.isNullOrEmpty()) {
                tvTechnicalSpecifications.text = product.technicalSpecifications
            } else {
                tvTechnicalSpecifications.text = "N/A"
            }

            // Total sold and reviews
            tvTotalSold.text = (product.totalSold ?: 0).toString()
            tvTotalReviews.text = (product.totalReviews ?: 0).toString()
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }
    }
}


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

    override fun setupViews() {
        val productId: Long = args.productId
        viewModel.loadProductDetails(productId)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnAddToCart.setOnClickListener {
            showToast("Added to cart!")
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


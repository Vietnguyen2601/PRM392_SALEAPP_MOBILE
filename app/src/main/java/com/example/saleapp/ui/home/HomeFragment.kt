package com.example.saleapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.saleapp.core.base.BaseFragment
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.core.utils.showToast
import com.example.saleapp.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHomeBinding =
        FragmentHomeBinding::inflate

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter

    override fun setupViews() {
        setupRecyclerView()
        viewModel.loadProducts()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter { product ->
            // Navigate to product detail
            val productId = product.productId ?: product.id ?: return@ProductAdapter
            val action = HomeFragmentDirections.actionHomeFragmentToProductDetailFragment(productId)
            findNavController().navigate(action)
        }

        binding.rvProducts.apply {
            adapter = productAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.productsState.collect { state ->
                when (state) {
                    is UiState.Loading -> showLoading(true)
                    is UiState.Success -> {
                        showLoading(false)
                        productAdapter.updateProducts(state.data)
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

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }
    }
}


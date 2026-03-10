package com.example.saleapp.ui.cart

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saleapp.core.base.BaseFragment
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.core.utils.showToast
import com.example.saleapp.data.model.response.CartResponse
import com.example.saleapp.databinding.FragmentCartBinding
import com.example.saleapp.ui.checkout.CheckoutActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class CartFragment : BaseFragment<FragmentCartBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCartBinding =
        FragmentCartBinding::inflate

    private val viewModel: CartViewModel by viewModels()
    private lateinit var cartItemAdapter: CartItemAdapter

    override fun setupViews() {
        setupRecyclerView()
        setupCheckoutButton()
        viewModel.loadCart()
    }

    private fun setupRecyclerView() {
        cartItemAdapter = CartItemAdapter { cartItem ->
            cartItem.cartItemId?.let { viewModel.removeItem(it) }
        }

        binding.rvCartItems.apply {
            adapter = cartItemAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupCheckoutButton() {
        binding.btnCheckout.setOnClickListener {
            startActivity(Intent(requireContext(), CheckoutActivity::class.java))
        }
    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.cartState.collectLatest { state ->
                        when (state) {
                            is UiState.Loading -> showLoading(true)
                            is UiState.Success -> {
                                showLoading(false)
                                updateCartUI(state.data)
                            }
                            is UiState.Error -> {
                                showLoading(false)
                                showToast(state.message)
                                showEmptyState(true)
                            }
                            else -> showLoading(false)
                        }
                    }
                }

                launch {
                    viewModel.removeItemState.collectLatest { state ->
                        when (state) {
                            is UiState.Loading -> {
                            }
                            is UiState.Success -> {
                                showToast("Item removed from cart")
                            }
                            is UiState.Error -> {
                                showToast("Failed to remove item: ${state.message}")
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun updateCartUI(cartResponse: CartResponse) {
        val itemsList = cartResponse.items ?: emptyList()
        if (itemsList.isEmpty()) {
            showEmptyState(true)
        } else {
            showEmptyState(false)
            cartItemAdapter.submitList(itemsList)
            
            // Update summary
            binding.tvTotalItems.text = cartResponse.getTotalItems().toString()
            binding.tvTotalAmount.text = String.format(Locale.US, "$%.2f", cartResponse.getTotalAmount())

            // Enable/disable checkout button
            binding.btnCheckout.isEnabled = itemsList.isNotEmpty()
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyState(show: Boolean) {
        binding.tvEmpty.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvCartItems.visibility = if (show) View.GONE else View.VISIBLE
        binding.cardSummary.visibility = if (show) View.GONE else View.VISIBLE
        binding.btnCheckout.visibility = if (show) View.GONE else View.VISIBLE
    }
}


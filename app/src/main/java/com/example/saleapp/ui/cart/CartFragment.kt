package com.example.saleapp.ui.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.saleapp.core.base.BaseFragment
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.core.utils.showToast
import com.example.saleapp.databinding.FragmentCartBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CartFragment : BaseFragment<FragmentCartBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCartBinding =
        FragmentCartBinding::inflate

    private val viewModel: CartViewModel by viewModels()

    override fun setupViews() {
        viewModel.loadCart()
    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.cartState.collect { state ->
                when (state) {
                    is UiState.Loading -> showLoading(true)
                    is UiState.Success -> {
                        showLoading(false)
                        // Update UI with cart data
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
        // Toggle progress bar visibility
    }
}


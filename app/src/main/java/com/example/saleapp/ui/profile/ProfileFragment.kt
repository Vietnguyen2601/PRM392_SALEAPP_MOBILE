package com.example.saleapp.ui.profile

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saleapp.core.base.BaseFragment
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.core.utils.gone
import com.example.saleapp.core.utils.showToast
import com.example.saleapp.core.utils.visible
import com.example.saleapp.databinding.FragmentProfileBinding
import com.example.saleapp.ui.auth.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentProfileBinding =
        FragmentProfileBinding::inflate

    private val viewModel: ProfileViewModel by viewModels()
    private val orderAdapter = OrderHistoryAdapter()

    /** Total pages returned by last successful API call */
    private var totalPages = 1

    override fun setupViews() {
        viewModel.loadProfile()
        viewModel.loadOrders(page = 1)

        // RecyclerView
        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
            isNestedScrollingEnabled = false
        }

        binding.btnLogout.setOnClickListener { showLogoutConfirmDialog() }

        binding.btnPrevPage.setOnClickListener { viewModel.prevPage() }
        binding.btnNextPage.setOnClickListener { viewModel.nextPage(totalPages) }
    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            // Profile
            launch {
                viewModel.profileState.collectLatest { state ->
                    when (state) {
                        is UiState.Loading -> showLoading(true)
                        is UiState.Success -> {
                            showLoading(false)
                            binding.tvName.text = state.data.username
                            binding.tvEmail.text = state.data.email
                        }
                    }
                }

            // Orders
            launch {
                viewModel.ordersState.collectLatest { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.rvOrders.gone()
                            binding.tvEmptyOrders.gone()
                            binding.layoutPaging.gone()
                        }
                        is UiState.Success -> {
                            val paged = state.data
                            totalPages = paged.totalPages

                            if (paged.items.isEmpty()) {
                                binding.rvOrders.gone()
                                binding.tvEmptyOrders.visible()
                                binding.layoutPaging.gone()
                            } else {
                                binding.tvEmptyOrders.gone()
                                binding.rvOrders.visible()
                                orderAdapter.submitList(paged.items)

                                // Paging controls — only show when more than 1 page
                                if (paged.totalPages > 1) {
                                    binding.layoutPaging.visible()
                                    binding.tvPageInfo.text = "${paged.page} / ${paged.totalPages}"
                                    binding.btnPrevPage.isEnabled = paged.page > 1
                                    binding.btnNextPage.isEnabled = paged.page < paged.totalPages
                                } else {
                                    binding.layoutPaging.gone()
                                }
                            }
                        }
                        is UiState.Error -> {
                            binding.rvOrders.gone()
                            binding.tvEmptyOrders.visible()
                            binding.tvEmptyOrders.text = "Không thể tải đơn hàng"
                            binding.layoutPaging.gone()
                        }
                        else -> Unit
                    }
                }
            }

            // Logout
            launch {
                viewModel.logoutState.collectLatest { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.btnLogout.isEnabled = false
                            showLoading(true)
                        }
                        is UiState.Success -> {
                            showLoading(false)
                            navigateToLogin()
                        }
                        is UiState.Error -> {
                            binding.btnLogout.isEnabled = true
                            showLoading(false)
                            showToast(state.message)
                        }
                        else -> {
                            binding.btnLogout.isEnabled = true
                            showLoading(false)
                        }
                    }
                }
            }
        }
    }

    private fun showLogoutConfirmDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ -> viewModel.logout() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLoading(show: Boolean) {
        if (show) binding.progressBar.visible() else binding.progressBar.gone()
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}



package com.example.saleapp.ui.profile

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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

    override fun setupViews() {
        viewModel.loadProfile()

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmDialog()
        }
    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            launch {
                viewModel.profileState.collectLatest { state ->
                    when (state) {
                        is UiState.Loading -> showLoading(true)
                        is UiState.Success -> {
                            showLoading(false)
                            val user = state.data
                            binding.tvName.text = user.username
                            binding.tvEmail.text = user.email
                        }
                        is UiState.Error -> {
                            showLoading(false)
                            showToast(state.message)
                        }
                        else -> showLoading(false)
                    }
                }
            }

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
            .setPositiveButton("Logout") { _, _ ->
                viewModel.logout()
            }
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


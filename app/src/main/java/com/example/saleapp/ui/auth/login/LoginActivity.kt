package com.example.saleapp.ui.auth.login

import android.content.Intent
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.saleapp.core.base.BaseActivity
import com.example.saleapp.core.utils.PreferenceManager
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.databinding.ActivityLoginBinding
import com.example.saleapp.ui.admin.AdminChatActivity
import com.example.saleapp.ui.main.MainActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityLoginBinding =
        ActivityLoginBinding::inflate

    private val viewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun setupViews() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.login(username, password)
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, com.example.saleapp.ui.auth.register.RegisterActivity::class.java))
            finish()
        }
    }

    override fun observeData() {
        lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is UiState.Loading -> showLoading(true)
                    is UiState.Success -> {
                        showLoading(false)
                        navigateAfterLogin(state.data.role)
                    }
                    is UiState.Error -> {
                        showLoading(false)
                        binding.root.let {
                            state.message?.let { it1 -> Snackbar.make(it, it1, Snackbar.LENGTH_SHORT) }
                                ?.show()
                        }
                    }
                    else -> showLoading(false)
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.btnLogin.isEnabled = !show
    }

    private fun navigateAfterLogin(role: String?) {
        val resolvedRole = role ?: preferenceManager.getUserRole()
        val isAdmin = resolvedRole.equals("Admin", ignoreCase = true) || resolvedRole.equals("Seller", ignoreCase = true)
        val destination = if (isAdmin) {
            Intent(this, AdminChatActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }
        startActivity(destination)
        finishAffinity()
    }
}


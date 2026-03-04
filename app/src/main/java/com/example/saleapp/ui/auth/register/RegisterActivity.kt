package com.example.saleapp.ui.auth.register

import android.content.Intent
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.saleapp.core.base.BaseActivity
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.databinding.ActivityRegisterBinding
import com.example.saleapp.ui.auth.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterActivity : BaseActivity<ActivityRegisterBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityRegisterBinding =
        ActivityRegisterBinding::inflate

    private val viewModel: RegisterViewModel by viewModels()

    override fun setupViews() {
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim().ifEmpty { null }
            viewModel.register(name, email, password, phone)
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun observeData() {
        lifecycleScope.launch {
            viewModel.registerState.collect { state ->
                when (state) {
                    is UiState.Loading -> showLoading(true)
                    is UiState.Success -> {
                        showLoading(false)
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finishAffinity()
                    }
                    is UiState.Error -> {
                        showLoading(false)
                        com.google.android.material.snackbar.Snackbar
                            .make(binding.root, state.message, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
                            .show()
                    }
                    else -> showLoading(false)
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.btnRegister.isEnabled = !show
    }
}


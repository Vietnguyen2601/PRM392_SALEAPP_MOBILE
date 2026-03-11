package com.example.saleapp.ui.admin

import android.view.LayoutInflater
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.saleapp.R
import com.example.saleapp.core.base.BaseActivity
import com.example.saleapp.databinding.ActivityAdminBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminActivity : BaseActivity<ActivityAdminBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityAdminBinding =
        ActivityAdminBinding::inflate

    override fun setupViews() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_admin) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationAdmin.setupWithNavController(navController)
    }

    override fun observeData() {}
}

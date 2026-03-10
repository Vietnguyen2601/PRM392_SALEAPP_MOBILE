package com.example.saleapp.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.saleapp.R
import com.example.saleapp.core.base.BaseActivity
import com.example.saleapp.core.notification.CartBadgeManager
import com.example.saleapp.core.worker.CartSyncWorker
import com.example.saleapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityMainBinding =
        ActivityMainBinding::inflate

    private val viewModel: MainViewModel by viewModels()
    private lateinit var navController: NavController
    
    @Inject
    lateinit var cartBadgeManager: CartBadgeManager
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, notification will work
            scheduleCartSyncWorker()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request notification permission for Android 13+
        requestNotificationPermission()
        
        // Handle notification click
        handleNotificationIntent()
    }

    override fun setupViews() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)
    }

    override fun observeData() {
        // Observe global app state if needed
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    scheduleCartSyncWorker()
                }
                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Below Android 13, no need to request permission
            scheduleCartSyncWorker()
        }
    }
    
    private fun scheduleCartSyncWorker() {
        val workRequest = PeriodicWorkRequestBuilder<CartSyncWorker>(
            15, TimeUnit.MINUTES // Sync every 15 minutes
        ).build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            CartSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent()
    }

    private fun handleNotificationIntent() {
        when (intent?.action) {
            "open_cart" -> navController.navigate(R.id.cartFragment)
            "open_home" -> {
                navController.popBackStack(R.id.homeFragment, false)
            }
        }
    }
}


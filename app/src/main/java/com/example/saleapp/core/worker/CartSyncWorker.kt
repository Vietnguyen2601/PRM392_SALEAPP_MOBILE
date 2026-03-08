package com.example.saleapp.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.saleapp.core.network.NetworkResult
import com.example.saleapp.core.notification.CartBadgeManager
import com.example.saleapp.data.repository.CartRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker to sync cart count periodically when app is closed
 * This ensures the notification badge stays updated even when the app is not running
 */
@HiltWorker
class CartSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val cartRepository: CartRepository,
    private val cartBadgeManager: CartBadgeManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Fetch cart from API
            when (val result = cartRepository.getCart()) {
                is NetworkResult.Success -> {
                    // Update cart badge with latest count
                    cartBadgeManager.updateCartBadge(result.data.getTotalItems())
                    Result.success()
                }
                is NetworkResult.Error -> {
                    // Retry on error
                    Result.retry()
                }
                is NetworkResult.Exception -> {
                    // Retry on exception
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            // Retry on any exception
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "cart_sync_work"
    }
}

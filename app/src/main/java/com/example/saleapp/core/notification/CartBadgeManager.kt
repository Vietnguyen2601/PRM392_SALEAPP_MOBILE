package com.example.saleapp.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.saleapp.R
import com.example.saleapp.SaleApplication
import com.example.saleapp.ui.main.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartBadgeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val NOTIFICATION_ID = 100
        private const val CHANNEL_ID = SaleApplication.CHANNEL_CART_ID
        private const val PREF_CART_COUNT = "cart_item_count"
    }

    private val prefs = context.getSharedPreferences("cart_badge_prefs", Context.MODE_PRIVATE)

    /**
     * Update cart badge count and show notification
     */
    fun updateCartBadge(itemCount: Int) {
        // Save count to preferences
        prefs.edit().putInt(PREF_CART_COUNT, itemCount).apply()
        
        // Show or update notification with badge
        if (itemCount > 0) {
            showCartNotification(itemCount)
        } else {
            clearCartNotification()
        }
    }

    /**
     * Get current cart count from preferences
     */
    fun getCartCount(): Int {
        return prefs.getInt(PREF_CART_COUNT, 0)
    }

    /**
     * Show notification with cart badge
     */
    private fun showCartNotification(itemCount: Int) {
        // Create intent to open app when notification is clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_cart", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Shopping Cart")
            .setContentText("You have $itemCount item(s) in your cart")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setNumber(itemCount) // This shows the badge on app icon
            .setOnlyAlertOnce(true) // Don't alert user again when updating
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Show notification
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission not granted
            android.util.Log.e("CartBadgeManager", "Notification permission not granted", e)
        }
    }

    /**
     * Clear cart notification
     */
    fun clearCartNotification() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        prefs.edit().putInt(PREF_CART_COUNT, 0).apply()
    }
}

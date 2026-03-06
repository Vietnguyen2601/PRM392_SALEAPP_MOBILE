package com.example.saleapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SaleApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        // Create notification channels
        createNotificationChannels()
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val cartChannel = NotificationChannel(
                CHANNEL_CART_ID,
                "Shopping Cart",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications about cart updates"
                setShowBadge(true)
            }

            val orderChannel = NotificationChannel(
                CHANNEL_ORDER_ID,
                "Orders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications about order updates"
            }

            val chatChannel = NotificationChannel(
                CHANNEL_CHAT_ID,
                "Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "New chat messages"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.apply {
                createNotificationChannel(cartChannel)
                createNotificationChannel(orderChannel)
                createNotificationChannel(chatChannel)
            }
        }
    }

    companion object {
        const val CHANNEL_CART_ID = "cart_channel"
        const val CHANNEL_ORDER_ID = "order_channel"
        const val CHANNEL_CHAT_ID = "chat_channel"
    }
}
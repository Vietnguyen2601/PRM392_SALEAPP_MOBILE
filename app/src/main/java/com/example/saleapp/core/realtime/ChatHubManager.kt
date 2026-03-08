package com.example.saleapp.core.realtime

import android.util.Log
import com.example.saleapp.data.model.response.ChatMessageDto
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState

class ChatHubManager(private val baseUrl: String, private val token: String) {

    private var hubConnection: HubConnection? = null
    private val TAG = "ChatHubManager"

    // Callbacks
    var onMessageReceived: ((ChatMessageDto) -> Unit)? = null
    var onMessageSent: ((ChatMessageDto) -> Unit)? = null
    var onMessageRead: ((Int) -> Unit)? = null
    var onUserTyping: ((Int, Int) -> Unit)? = null // conversationId, userId
    var onShopTyping: ((Int) -> Unit)? = null // conversationId
    var onConnectionStateChanged: ((HubConnectionState) -> Unit)? = null

    fun initialize() {
        try {
            hubConnection = HubConnectionBuilder.create("$baseUrl/hubs/chat?access_token=$token")
                .build()

            setupEventHandlers()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize hub: ${e.message}", e)
        }
    }

    private fun setupEventHandlers() {
        hubConnection?.apply {
            // ReceiveMessage event
            on("ReceiveMessage", { message: ChatMessageDto ->
                Log.d(TAG, "📨 ReceiveMessage: $message")
                onMessageReceived?.invoke(message)
            }, ChatMessageDto::class.java)

            // MessageSent event
            on("MessageSent", { message: ChatMessageDto ->
                Log.d(TAG, "✅ MessageSent: $message")
                onMessageSent?.invoke(message)
            }, ChatMessageDto::class.java)

            // MessageRead event
            on("MessageRead", { messageId: Int ->
                Log.d(TAG, "👁️ MessageRead: $messageId")
                onMessageRead?.invoke(messageId)
            }, Int::class.java)

            // UserTyping event (for admin/seller)
            on("UserTyping", { conversationId: Int, userId: Int ->
                Log.d(TAG, "⌨️ UserTyping: conversationId=$conversationId, userId=$userId")
                onUserTyping?.invoke(conversationId, userId)
            }, Int::class.java, Int::class.java)

            // ShopTyping event (for users)
            on("ShopTyping", { conversationId: Int ->
                Log.d(TAG, "🏪 ShopTyping: conversationId=$conversationId")
                onShopTyping?.invoke(conversationId)
            }, Int::class.java)

            // Connection state callbacks
            onClosed { exception ->
                Log.w(TAG, "❌ Connection closed: ${exception?.message}")
                onConnectionStateChanged?.invoke(HubConnectionState.DISCONNECTED)
            }
        }
    }

    suspend fun connect(): Result<Unit> {
        return try {
            if (hubConnection?.connectionState == HubConnectionState.CONNECTED) {
                Log.w(TAG, "Already connected")
                return Result.success(Unit)
            }

            Log.d(TAG, "🔌 Connecting to ChatHub...")
            hubConnection?.start()
            Log.d(TAG, "✅ Connected to ChatHub")
            onConnectionStateChanged?.invoke(HubConnectionState.CONNECTED)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Connection failed: ${e.message}", e)
            onConnectionStateChanged?.invoke(HubConnectionState.DISCONNECTED)
            Result.failure(e)
        }
    }

    suspend fun disconnect(): Result<Unit> {
        return try {
            hubConnection?.stop()
            Log.d(TAG, "🔌 Disconnected from ChatHub")
            onConnectionStateChanged?.invoke(HubConnectionState.DISCONNECTED)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun sendMessage(conversationId: Int, message: String): Result<Unit> {
        return try {
            Log.d(TAG, "📤 Sending message to conversation $conversationId...")
            hubConnection?.send("SendMessage", conversationId, message)
            Log.d(TAG, "✅ Message sent")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to send message: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun markAsRead(messageId: Int): Result<Unit> {
        return try {
            hubConnection?.send("MarkAsRead", messageId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark as read: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun sendTypingIndicator(conversationId: Int): Result<Unit> {
        return try {
            hubConnection?.send("UserTyping", conversationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send typing indicator: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun isConnected(): Boolean {
        return hubConnection?.connectionState == HubConnectionState.CONNECTED
    }
}


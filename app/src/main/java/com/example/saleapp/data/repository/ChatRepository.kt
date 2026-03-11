package com.example.saleapp.data.repository

import com.example.saleapp.core.network.ChatApiService
import com.example.saleapp.core.network.NetworkResult
import com.example.saleapp.core.realtime.ChatHubManager
import com.example.saleapp.data.model.request.SendMessageRequest
import com.example.saleapp.data.model.response.ChatConversationDto
import com.example.saleapp.data.model.response.ChatMessageDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatApiService: ChatApiService,
    private val chatHubManager: ChatHubManager
) {

    // REST API Methods
    suspend fun getConversations(): NetworkResult<List<ChatConversationDto>> = withContext(Dispatchers.IO) {
        try {
            val response = chatApiService.getConversations()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error(response.code(), "Failed to get conversations: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getConversation(id: Int): NetworkResult<ChatConversationDto> = withContext(Dispatchers.IO) {
        try {
            val response = chatApiService.getConversation(id)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error(response.code(), "Failed to get conversation: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun createConversation(): NetworkResult<ChatConversationDto> = withContext(Dispatchers.IO) {
        try {
            val response = chatApiService.createConversation()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error(response.code(), "Failed to create conversation: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getMessages(
        conversationId: Int,
        skip: Int = 0,
        take: Int = 50
    ): NetworkResult<List<ChatMessageDto>> = withContext(Dispatchers.IO) {
        try {
            val response = chatApiService.getMessages(conversationId, skip, take)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error(response.code(), "Failed to get messages: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun sendMessageRest(conversationId: Int, message: String): NetworkResult<ChatMessageDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = chatApiService.sendMessage(conversationId, SendMessageRequest(message))
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    NetworkResult.Error(response.code(), "Failed to send message: ${response.code()}")
                }
            } catch (e: Exception) {
                NetworkResult.Exception(e)
            }
        }

    suspend fun markAsReadRest(messageId: Int): NetworkResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = chatApiService.markMessageAsRead(messageId)
            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(response.code(), "Failed to mark as read: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    // SignalR Methods
    suspend fun connectToChat(): Result<Unit> {
        return chatHubManager.connect()
    }

    suspend fun disconnectFromChat(): Result<Unit> {
        return chatHubManager.disconnect()
    }

    suspend fun sendMessageRealtime(conversationId: Int, message: String): Result<Unit> {
        return chatHubManager.sendMessage(conversationId, message)
    }

    suspend fun markAsReadRealtime(messageId: Int): Result<Unit> {
        return chatHubManager.markAsRead(messageId)
    }

    suspend fun sendTypingIndicator(conversationId: Int): Result<Unit> {
        return chatHubManager.sendTypingIndicator(conversationId)
    }

    fun isConnected(): Boolean = chatHubManager.isConnected()

    fun setMessageReceivedListener(listener: (ChatMessageDto) -> Unit) {
        chatHubManager.onMessageReceived = listener
    }

    fun setMessageSentListener(listener: (ChatMessageDto) -> Unit) {
        chatHubManager.onMessageSent = listener
    }

    fun setMessageReadListener(listener: (Int) -> Unit) {
        chatHubManager.onMessageRead = listener
    }

    fun setShopTypingListener(listener: (Int) -> Unit) {
        chatHubManager.onShopTyping = listener
    }

    fun setUserTypingListener(listener: (conversationId: Int, userId: Int) -> Unit) {
        chatHubManager.onUserTyping = listener
    }

    suspend fun closeConversation(conversationId: Int): NetworkResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = chatApiService.closeConversation(conversationId)
            if (response.isSuccessful) NetworkResult.Success(Unit)
            else NetworkResult.Error(response.code(), "Failed to close conversation")
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }
}


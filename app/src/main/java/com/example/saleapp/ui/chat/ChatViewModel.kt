package com.example.saleapp.ui.chat

import androidx.lifecycle.viewModelScope
import com.example.saleapp.core.base.BaseViewModel
import com.example.saleapp.core.network.NetworkResult
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.data.model.response.ChatConversationDto
import com.example.saleapp.data.model.response.ChatMessageDto
import com.example.saleapp.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : BaseViewModel() {

    private val _conversationState = MutableStateFlow<UiState<ChatConversationDto>>(UiState.Idle)
    val conversationState: StateFlow<UiState<ChatConversationDto>> = _conversationState

    private val _messagesState = MutableStateFlow<UiState<List<ChatMessageDto>>>(UiState.Idle)
    val messagesState: StateFlow<UiState<List<ChatMessageDto>>> = _messagesState

    private val _messages = MutableStateFlow<List<ChatMessageDto>>(emptyList())
    val messages: StateFlow<List<ChatMessageDto>> = _messages

    private val _newMessage = MutableStateFlow<ChatMessageDto?>(null)
    val newMessage: StateFlow<ChatMessageDto?> = _newMessage

    private val _connectionState = MutableStateFlow<String>("Disconnected")
    val connectionState: StateFlow<String> = _connectionState

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    private val _sendMessageState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val sendMessageState: StateFlow<UiState<Unit>> = _sendMessageState

    private var tempMessageId = -1

    init {
        setupRealtimeListeners()
    }

    private fun setupRealtimeListeners() {
        chatRepository.setMessageReceivedListener { message ->
            _newMessage.value = message
            // Skip "User" messages — those are the user's own messages, already handled
            // by the optimistic update + MessageSent listener to avoid duplication.
            if (message.senderType == "User") return@setMessageReceivedListener
            val currentMessages = _messages.value.toMutableList()
            val existingIndex = currentMessages.indexOfFirst { it.chatMessageId == message.chatMessageId }
            if (existingIndex == -1) {
                currentMessages.add(message)
                _messages.value = currentMessages
            }
        }

        chatRepository.setMessageSentListener { message ->
            // Message was sent successfully, replace temp message with real one
            _newMessage.value = message
            val currentMessages = _messages.value.toMutableList()

            // Find and remove ALL temporary messages (with ID 0 or negative) that have the same content
            val tempIndex = currentMessages.indexOfLast {
                (it.chatMessageId <= 0) && it.message == message.message && it.senderType == "User"
            }
            if (tempIndex != -1) {
                currentMessages.removeAt(tempIndex)
            }

            // Add the real message from server (if not already exists)
            val existingIndex = currentMessages.indexOfFirst { it.chatMessageId == message.chatMessageId }
            if (existingIndex == -1) {
                currentMessages.add(message)
                _messages.value = currentMessages
            }

            _sendMessageState.value = UiState.Success(Unit)
        }

        chatRepository.setMessageReadListener { messageId ->
            // Update read status in messages list
            val currentMessages = _messages.value.toMutableList()
            val index = currentMessages.indexOfFirst { it.chatMessageId == messageId }
            if (index != -1) {
                // Note: We would need to update the readAt field, but it's not mutable
                // For now, just trigger a refresh
            }
        }

        chatRepository.setShopTypingListener { conversationId ->
            _isTyping.value = true
            // Reset typing indicator after 3 seconds
            viewModelScope.launch {
                kotlinx.coroutines.delay(3000)
                _isTyping.value = false
            }
        }
    }

    fun connectToChat() {
        viewModelScope.launch(exceptionHandler) {
            try {
                _connectionState.value = "Connecting..."
                chatRepository.connectToChat()
                    .onSuccess {
                        _connectionState.value = "Connected"
                    }
                    .onFailure { error ->
                        _connectionState.value = "Disconnected"
                    }
            } catch (e: Exception) {
                _connectionState.value = "Disconnected"
            }
        }
    }

    fun disconnectFromChat() {
        viewModelScope.launch(exceptionHandler) {
            chatRepository.disconnectFromChat()
            _connectionState.value = "Disconnected"
        }
    }

    fun createOrGetConversation() {
        _conversationState.value = UiState.Loading
        viewModelScope.launch(exceptionHandler) {
            when (val result = chatRepository.createConversation()) {
                is NetworkResult.Success -> {
                    _conversationState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _conversationState.value = UiState.Error(result.message ?: "Unknown error")
                }
                is NetworkResult.Exception -> {
                    _conversationState.value = UiState.Error(result.e.message ?: "Unknown error")
                }
            }
        }
    }

    fun loadMessages(conversationId: Int) {
        _messagesState.value = UiState.Loading
        viewModelScope.launch(exceptionHandler) {
            when (val result = chatRepository.getMessages(conversationId)) {
                is NetworkResult.Success -> {
                    _messages.value = result.data
                    _messagesState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _messagesState.value = UiState.Error(result.message ?: "Unknown error")
                }
                is NetworkResult.Exception -> {
                    _messagesState.value = UiState.Error(result.e.message ?: "Unknown error")
                }
            }
        }
    }

    fun sendMessage(conversationId: Int, message: String) {
        if (message.isBlank()) return

        _sendMessageState.value = UiState.Loading
        viewModelScope.launch(exceptionHandler) {
            try {
                if (chatRepository.isConnected()) {
                    // Use SignalR for real-time sending
                    // Optimistically add message to UI immediately with unique negative ID
                    val tempMessage = ChatMessageDto(
                        chatMessageId = tempMessageId--,
                        conversationId = conversationId,
                        senderType = "User",
                        message = message,
                        sentAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                            .format(java.util.Date()),
                        readAt = null
                    )
                    val currentMessages = _messages.value.toMutableList()
                    currentMessages.add(tempMessage)
                    _messages.value = currentMessages

                    chatRepository.sendMessageRealtime(conversationId, message)
                        .onSuccess {
                            // Success will be handled by MessageSent callback
                            // The callback will update the message with the real ID from server
                        }
                        .onFailure { error ->
                            // Remove the temporary message on failure
                            val updatedMessages = _messages.value.toMutableList()
                            updatedMessages.removeAll { it.chatMessageId == tempMessage.chatMessageId }
                            _messages.value = updatedMessages
                            _sendMessageState.value = UiState.Error(error.message ?: "Failed to send")
                        }
                } else {
                    // Fallback to REST API
                    when (val result = chatRepository.sendMessageRest(conversationId, message)) {
                        is NetworkResult.Success -> {
                            val currentMessages = _messages.value.toMutableList()
                            // Check if message already exists (shouldn't happen but be safe)
                            val existingIndex = currentMessages.indexOfFirst {
                                it.chatMessageId == result.data.chatMessageId
                            }
                            if (existingIndex == -1) {
                                currentMessages.add(result.data)
                                _messages.value = currentMessages
                            }
                            _sendMessageState.value = UiState.Success(Unit)
                        }
                        is NetworkResult.Error -> {
                            _sendMessageState.value = UiState.Error(result.message ?: "Unknown error")
                        }
                        is NetworkResult.Exception -> {
                            _sendMessageState.value = UiState.Error(result.e.message ?: "Unknown error")
                        }
                    }
                }
            } catch (e: Exception) {
                _sendMessageState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun markAsRead(messageId: Int) {
        viewModelScope.launch(exceptionHandler) {
            if (chatRepository.isConnected()) {
                chatRepository.markAsReadRealtime(messageId)
            } else {
                chatRepository.markAsReadRest(messageId)
            }
        }
    }

    fun sendTypingIndicator(conversationId: Int) {
        viewModelScope.launch(exceptionHandler) {
            if (chatRepository.isConnected()) {
                chatRepository.sendTypingIndicator(conversationId)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            chatRepository.disconnectFromChat()
        }
    }
}


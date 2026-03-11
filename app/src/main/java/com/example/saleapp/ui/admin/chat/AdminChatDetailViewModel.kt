package com.example.saleapp.ui.admin.chat

import androidx.lifecycle.viewModelScope
import com.example.saleapp.core.base.BaseViewModel
import com.example.saleapp.core.network.NetworkResult
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.data.model.response.ChatMessageDto
import com.example.saleapp.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminChatDetailViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : BaseViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessageDto>>(emptyList())
    val messages: StateFlow<List<ChatMessageDto>> = _messages

    private val _connectionState = MutableStateFlow("Disconnected")
    val connectionState: StateFlow<String> = _connectionState

    /** True when the user on the other end is typing */
    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    private val _sendMessageState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val sendMessageState: StateFlow<UiState<Unit>> = _sendMessageState

    /** The conversation currently displayed — used to filter incoming realtime events */
    private var currentConversationId: Int = 0

    init {
        setupRealtimeListeners()
    }

    private fun setupRealtimeListeners() {
        // ReceiveMessage: admin is in shop_admin group so it receives msgs from ALL convos.
        // Filter to only show messages for the currently open conversation.
        chatRepository.setMessageReceivedListener { message ->
            if (message.conversationId != currentConversationId) return@setMessageReceivedListener
            val current = _messages.value.toMutableList()
            if (current.none { it.chatMessageId == message.chatMessageId }) {
                current.add(message)
                _messages.value = current
            }
            // Mark as read immediately when admin sees the message
            markMessageAsRead(message.chatMessageId)
        }

        // MessageSent: confirmation that our optimistic message was saved on the server
        chatRepository.setMessageSentListener { message ->
            if (message.conversationId != currentConversationId) return@setMessageSentListener
            val current = _messages.value.toMutableList()
            val tempIndex = current.indexOfLast {
                it.chatMessageId <= 0 && it.message == message.message && it.senderType == "Shop"
            }
            if (tempIndex != -1) current.removeAt(tempIndex)
            if (current.none { it.chatMessageId == message.chatMessageId }) current.add(message)
            _messages.value = current
            _sendMessageState.value = UiState.Success(Unit)
        }

        chatRepository.setMessageReadListener { /* keep in sync if needed */ }

        // UserTyping: server fires this when the USER (customer) is typing → show indicator to admin
        chatRepository.setUserTypingListener { conversationId, _ ->
            if (conversationId != currentConversationId) return@setUserTypingListener
            _isTyping.value = true
            viewModelScope.launch {
                kotlinx.coroutines.delay(3000)
                _isTyping.value = false
            }
        }
    }

    fun connectToChat() {
        viewModelScope.launch(exceptionHandler) {
            _connectionState.value = "Connecting..."
            chatRepository.connectToChat()
                .onSuccess { _connectionState.value = "Connected" }
                .onFailure { _connectionState.value = "Disconnected" }
        }
    }

    fun loadMessages(conversationId: Int) {
        currentConversationId = conversationId
        viewModelScope.launch(exceptionHandler) {
            when (val result = chatRepository.getMessages(conversationId)) {
                is NetworkResult.Success -> {
                    _messages.value = result.data
                    // Mark all unread messages from users as read
                    result.data
                        .filter { it.readAt == null && it.senderType == "User" }
                        .forEach { markMessageAsRead(it.chatMessageId) }
                }
                is NetworkResult.Error -> { /* leave list empty, no error toast needed */ }
                is NetworkResult.Exception -> { /* same */ }
            }
        }
    }

    private fun markMessageAsRead(messageId: Int) {
        viewModelScope.launch(exceptionHandler) {
            chatRepository.markAsReadRest(messageId)
        }
    }

    fun sendMessage(conversationId: Int, message: String) {
        if (message.isBlank()) return
        _sendMessageState.value = UiState.Loading
        viewModelScope.launch(exceptionHandler) {
            if (chatRepository.isConnected()) {
                // Optimistic UI update
                val tempMessage = ChatMessageDto(
                    chatMessageId = -(System.currentTimeMillis().toInt()),
                    conversationId = conversationId,
                    senderType = "Shop",
                    message = message,
                    sentAt = java.text.SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss",
                        java.util.Locale.getDefault()
                    ).format(java.util.Date()),
                    readAt = null
                )
                val current = _messages.value.toMutableList()
                current.add(tempMessage)
                _messages.value = current

                chatRepository.sendMessageRealtime(conversationId, message)
                    .onFailure { error ->
                        // Remove optimistic message on failure
                        val updated = _messages.value.toMutableList()
                        updated.removeAll { it.chatMessageId == tempMessage.chatMessageId }
                        _messages.value = updated
                        _sendMessageState.value = UiState.Error(error.message ?: "Gửi thất bại")
                    }
            } else {
                // Fallback: REST
                when (val result = chatRepository.sendMessageRest(conversationId, message)) {
                    is NetworkResult.Success -> {
                        val current = _messages.value.toMutableList()
                        if (current.none { it.chatMessageId == result.data.chatMessageId }) {
                            current.add(result.data)
                            _messages.value = current
                        }
                        _sendMessageState.value = UiState.Success(Unit)
                    }
                    is NetworkResult.Error -> _sendMessageState.value = UiState.Error(result.message ?: "Thất bại")
                    is NetworkResult.Exception -> _sendMessageState.value = UiState.Error(result.e.message ?: "Lỗi")
                }
            }
        }
    }

    fun sendTypingIndicator(conversationId: Int) {
        viewModelScope.launch(exceptionHandler) {
            chatRepository.sendTypingIndicator(conversationId)
        }
    }

    fun closeConversation(conversationId: Int, onDone: () -> Unit) {
        viewModelScope.launch(exceptionHandler) {
            chatRepository.closeConversation(conversationId)
            onDone()
        }
    }

    fun disconnectFromChat() {
        viewModelScope.launch(exceptionHandler) {
            chatRepository.disconnectFromChat()
            _connectionState.value = "Disconnected"
        }
    }

    override fun handleError(throwable: Throwable) {
        _sendMessageState.value = UiState.Error(throwable.message ?: "Unknown error")
    }
}

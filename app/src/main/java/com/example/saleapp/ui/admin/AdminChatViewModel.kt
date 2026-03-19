package com.example.saleapp.ui.admin

import androidx.lifecycle.viewModelScope
import com.example.saleapp.core.base.BaseViewModel
import com.example.saleapp.core.network.NetworkResult
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.data.model.response.ChatConversationDto
import com.example.saleapp.data.model.response.ChatMessageDto
import com.example.saleapp.data.repository.AuthRepository
import com.example.saleapp.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : BaseViewModel() {

    private val _conversationsState = MutableStateFlow<UiState<List<ChatConversationDto>>>(UiState.Idle)
    val conversationsState: StateFlow<UiState<List<ChatConversationDto>>> = _conversationsState

    private val _selectedConversation = MutableStateFlow<ChatConversationDto?>(null)
    val selectedConversation: StateFlow<ChatConversationDto?> = _selectedConversation

    private val _messages = MutableStateFlow<List<ChatMessageDto>>(emptyList())
    val messages: StateFlow<List<ChatMessageDto>> = _messages

    private val _connectionState = MutableStateFlow("Disconnected")
    val connectionState: StateFlow<String> = _connectionState

    private val _isUserTypingConversation = MutableStateFlow<Int?>(null)
    val isUserTypingConversation: StateFlow<Int?> = _isUserTypingConversation

    private val _sendMessageState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val sendMessageState: StateFlow<UiState<Unit>> = _sendMessageState

    private val _logoutState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val logoutState: StateFlow<UiState<Unit>> = _logoutState

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var tempMessageId = -1

    init {
        setupRealtimeListeners()
    }

    private fun setupRealtimeListeners() {
        chatRepository.setMessageReceivedListener { message ->
            if (_selectedConversation.value?.conversationId == message.conversationId) {
                val updatedMessages = _messages.value.toMutableList()
                updatedMessages.add(message)
                _messages.value = updatedMessages
                markAsRead(message.chatMessageId)
            }
            // Refresh conversations to reflect latest message/unread state
            loadConversations()
        }

        chatRepository.setMessageSentListener { message ->
            if (_selectedConversation.value?.conversationId == message.conversationId) {
                val updatedMessages = _messages.value.toMutableList()
                val tempIndex = updatedMessages.indexOfLast {
                    it.chatMessageId <= 0 && it.message == message.message && it.senderType.equals("Shop", ignoreCase = true)
                }
                if (tempIndex != -1) {
                    updatedMessages.removeAt(tempIndex)
                }
                if (updatedMessages.none { it.chatMessageId == message.chatMessageId }) {
                    updatedMessages.add(message)
                }
                _messages.value = updatedMessages
            }
            _sendMessageState.value = UiState.Success(Unit)
        }

        chatRepository.setMessageReadListener { messageId ->
            // No-op for now; could be used to update UI badge
        }

        chatRepository.setUserTypingListener { conversationId, _ ->
            if (_selectedConversation.value?.conversationId == conversationId) {
                _isUserTypingConversation.value = conversationId
                viewModelScope.launch {
                    kotlinx.coroutines.delay(2500)
                    if (_selectedConversation.value?.conversationId == conversationId) {
                        _isUserTypingConversation.value = null
                    }
                }
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

    fun disconnectFromChat() {
        viewModelScope.launch(exceptionHandler) {
            chatRepository.disconnectFromChat()
            _connectionState.value = "Disconnected"
        }
    }

    fun loadConversations() {
        _conversationsState.value = UiState.Loading
        viewModelScope.launch(exceptionHandler) {
            when (val result = chatRepository.getConversations()) {
                is NetworkResult.Success -> _conversationsState.value = UiState.Success(result.data)
                is NetworkResult.Error -> {
                    _conversationsState.value = UiState.Error(result.message ?: "Failed to load conversations")
                    _error.value = result.message
                }
                is NetworkResult.Exception -> {
                    _conversationsState.value = UiState.Error(result.e.message ?: "Failed to load conversations")
                    _error.value = result.e.message
                }
            }
        }
    }

    fun selectConversation(conversation: ChatConversationDto) {
        _selectedConversation.value = conversation
        loadMessages(conversation.conversationId)
    }

    fun loadMessages(conversationId: Int) {
        viewModelScope.launch(exceptionHandler) {
            when (val result = chatRepository.getMessages(conversationId)) {
                is NetworkResult.Success -> _messages.value = result.data
                is NetworkResult.Error -> _error.value = result.message ?: "Failed to load messages"
                is NetworkResult.Exception -> _error.value = result.e.message ?: "Failed to load messages"
            }
        }
    }

    fun sendMessage(conversationId: Int, message: String) {
        if (message.isBlank()) return

        _sendMessageState.value = UiState.Loading
        viewModelScope.launch(exceptionHandler) {
            try {
                if (chatRepository.isConnected()) {
                    val tempMessage = ChatMessageDto(
                        chatMessageId = tempMessageId--,
                        conversationId = conversationId,
                        senderType = "Shop",
                        message = message,
                        sentAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                            .format(java.util.Date()),
                        readAt = null
                    )
                    val updatedMessages = _messages.value.toMutableList()
                    updatedMessages.add(tempMessage)
                    _messages.value = updatedMessages

                    chatRepository.sendMessageRealtime(conversationId, message)
                        .onSuccess { /* handled in MessageSent */ }
                        .onFailure { error ->
                            val cleaned = _messages.value.toMutableList().apply {
                                removeAll { it.chatMessageId == tempMessage.chatMessageId }
                            }
                            _messages.value = cleaned
                            _sendMessageState.value = UiState.Error(error.message ?: "Failed to send message")
                            _error.value = error.message
                        }
                } else {
                    when (val result = chatRepository.sendMessageRest(conversationId, message)) {
                        is NetworkResult.Success -> {
                            val updatedMessages = _messages.value.toMutableList()
                            if (updatedMessages.none { it.chatMessageId == result.data.chatMessageId }) {
                                updatedMessages.add(result.data)
                            }
                            _messages.value = updatedMessages
                            _sendMessageState.value = UiState.Success(Unit)
                        }
                        is NetworkResult.Error -> {
                            _sendMessageState.value = UiState.Error(result.message ?: "Failed to send message")
                            _error.value = result.message
                        }
                        is NetworkResult.Exception -> {
                            _sendMessageState.value = UiState.Error(result.e.message ?: "Failed to send message")
                            _error.value = result.e.message
                        }
                    }
                }
            } catch (e: Exception) {
                _sendMessageState.value = UiState.Error(e.message ?: "Failed to send message")
                _error.value = e.message
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

    fun logout() {
        _logoutState.value = UiState.Loading
        viewModelScope.launch(exceptionHandler) {
            when (val result = authRepository.logout()) {
                is NetworkResult.Success -> _logoutState.value = UiState.Success(Unit)
                is NetworkResult.Error -> _logoutState.value = UiState.Error(result.message ?: "Logout failed")
                is NetworkResult.Exception -> _logoutState.value = UiState.Error(result.e.message ?: "Logout failed")
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

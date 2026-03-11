package com.example.saleapp.ui.admin.conversations

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
class AdminConversationsViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : BaseViewModel() {

    private val _conversationsState = MutableStateFlow<UiState<List<ChatConversationDto>>>(UiState.Idle)
    val conversationsState: StateFlow<UiState<List<ChatConversationDto>>> = _conversationsState

    init {
        // Listen for incoming messages from any conversation and update the list in real-time
        chatRepository.setMessageReceivedListener { message ->
            updateConversationWithNewMessage(message)
        }
    }

    fun loadConversations() {
        _conversationsState.value = UiState.Loading
        viewModelScope.launch(exceptionHandler) {
            when (val result = chatRepository.getConversations()) {
                is NetworkResult.Success -> _conversationsState.value = UiState.Success(result.data)
                is NetworkResult.Error -> _conversationsState.value = UiState.Error(result.message ?: "Không thể tải cuộc trò chuyện")
                is NetworkResult.Exception -> _conversationsState.value = UiState.Error(result.e.message ?: "Lỗi kết nối")
            }
        }
    }

    fun connectToSignalR() {
        viewModelScope.launch(exceptionHandler) {
            chatRepository.connectToChat()
        }
    }

    /**
     * When a new message arrives via SignalR, update the corresponding conversation
     * in the list: bump lastMessage and increment unreadCount.
     */
    private fun updateConversationWithNewMessage(message: ChatMessageDto) {
        val current = _conversationsState.value
        if (current !is UiState.Success) return

        val updated = current.data.map { conv ->
            if (conv.conversationId == message.conversationId) {
                conv.copy(
                    lastMessage = message,
                    unreadCount = conv.unreadCount + 1
                )
            } else conv
        }
        // Bring the updated conversation to the top
        val sorted = updated.sortedByDescending {
            it.lastMessage?.sentAt ?: it.lastMessageAt ?: it.createdAt
        }
        _conversationsState.value = UiState.Success(sorted)
    }

    override fun handleError(throwable: Throwable) {
        _conversationsState.value = UiState.Error(throwable.message ?: "Unknown error")
    }
}

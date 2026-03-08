package com.example.saleapp.data.model.response

data class ChatConversationDto(
    val conversationId: Int,
    val userId: Int,
    val username: String,
    val status: String, // "Open" or "Closed"
    val lastMessageAt: String?, // ISO 8601 datetime
    val createdAt: String,
    val lastMessage: ChatMessageDto?,
    val unreadCount: Int
)



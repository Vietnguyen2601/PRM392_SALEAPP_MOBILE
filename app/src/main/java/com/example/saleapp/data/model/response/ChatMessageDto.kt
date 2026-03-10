package com.example.saleapp.data.model.response

data class ChatMessageDto(
    val chatMessageId: Int,
    val conversationId: Int,
    val senderType: String, // "User" or "Shop"
    val message: String,
    val sentAt: String, // ISO 8601 datetime
    val readAt: String? // ISO 8601 datetime or null
)


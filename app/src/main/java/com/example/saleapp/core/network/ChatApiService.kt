package com.example.saleapp.core.network

import com.example.saleapp.data.model.request.SendMessageRequest
import com.example.saleapp.data.model.response.ChatConversationDto
import com.example.saleapp.data.model.response.ChatMessageDto
import com.example.saleapp.data.model.response.UnreadCountResponse
import retrofit2.Response
import retrofit2.http.*

interface ChatApiService {

    @GET("Chat/conversations")
    suspend fun getConversations(): Response<List<ChatConversationDto>>

    @GET("Chat/conversations/{id}")
    suspend fun getConversation(@Path("id") id: Int): Response<ChatConversationDto>

    @POST("Chat/conversations")
    suspend fun createConversation(): Response<ChatConversationDto>

    @GET("Chat/conversations/{id}/messages")
    suspend fun getMessages(
        @Path("id") conversationId: Int,
        @Query("skip") skip: Int = 0,
        @Query("take") take: Int = 50
    ): Response<List<ChatMessageDto>>

    @POST("Chat/conversations/{id}/messages")
    suspend fun sendMessage(
        @Path("id") conversationId: Int,
        @Body request: SendMessageRequest
    ): Response<ChatMessageDto>

    @PUT("Chat/messages/{id}/read")
    suspend fun markMessageAsRead(@Path("id") messageId: Int): Response<Unit>

    @GET("Chat/conversations/{id}/unread-count")
    suspend fun getUnreadCount(@Path("id") conversationId: Int): Response<UnreadCountResponse>

    @PUT("Chat/conversations/{id}/close")
    suspend fun closeConversation(@Path("id") conversationId: Int): Response<Unit>
}


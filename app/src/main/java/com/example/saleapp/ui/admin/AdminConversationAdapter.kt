package com.example.saleapp.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.saleapp.data.model.response.ChatConversationDto
import com.example.saleapp.databinding.ItemAdminConversationBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AdminConversationAdapter(
    private val onConversationClick: (ChatConversationDto) -> Unit
) : ListAdapter<ChatConversationDto, AdminConversationAdapter.ConversationViewHolder>(Diff) {

    private var selectedConversationId: Int? = null

    fun setSelectedConversation(conversationId: Int?) {
        selectedConversationId = conversationId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding = ItemAdminConversationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConversationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ConversationViewHolder(
        private val binding: ItemAdminConversationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatConversationDto) {
            binding.tvUserName.text = item.username
            binding.tvLastMessage.text = item.lastMessage?.message ?: "No messages yet"
            binding.tvTimestamp.text = formatTime(item.lastMessageAt ?: item.createdAt)
            binding.tvUnreadCount.apply {
                visibility = if (item.unreadCount > 0) View.VISIBLE else View.GONE
                text = item.unreadCount.toString()
            }
            val isSelected = item.conversationId == selectedConversationId
            binding.cardContainer.strokeWidth = if (isSelected) 3 else 0
            binding.cardContainer.setOnClickListener { onConversationClick(item) }
        }
    }

    private fun formatTime(timestamp: String?): String {
        if (timestamp.isNullOrBlank()) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
            val date = inputFormat.parse(timestamp)
            date?.let { outputFormat.format(it) } ?: timestamp
        } catch (_: Exception) {
            timestamp
        }
    }

    private object Diff : DiffUtil.ItemCallback<ChatConversationDto>() {
        override fun areItemsTheSame(oldItem: ChatConversationDto, newItem: ChatConversationDto): Boolean =
            oldItem.conversationId == newItem.conversationId

        override fun areContentsTheSame(oldItem: ChatConversationDto, newItem: ChatConversationDto): Boolean =
            oldItem == newItem
    }
}

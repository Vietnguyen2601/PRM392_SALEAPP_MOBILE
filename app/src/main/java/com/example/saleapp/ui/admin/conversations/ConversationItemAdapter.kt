package com.example.saleapp.ui.admin.conversations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.saleapp.data.model.response.ChatConversationDto
import com.example.saleapp.databinding.ItemConversationBinding

class ConversationItemAdapter(
    private val onItemClick: (ChatConversationDto) -> Unit
) : ListAdapter<ChatConversationDto, ConversationItemAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConversationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemConversationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatConversationDto) {
            binding.tvUsername.text = item.username
            binding.tvLastMessage.text = item.lastMessage?.message ?: "Chưa có tin nhắn"
            binding.tvStatus.text = if (item.status == "Open") "Đang mở" else "Đã đóng"

            if (item.unreadCount > 0) {
                binding.tvUnread.visibility = View.VISIBLE
                binding.tvUnread.text = if (item.unreadCount > 99) "99+" else item.unreadCount.toString()
            } else {
                binding.tvUnread.visibility = View.GONE
            }

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChatConversationDto>() {
        override fun areItemsTheSame(oldItem: ChatConversationDto, newItem: ChatConversationDto) =
            oldItem.conversationId == newItem.conversationId

        override fun areContentsTheSame(oldItem: ChatConversationDto, newItem: ChatConversationDto) =
            oldItem == newItem
    }
}

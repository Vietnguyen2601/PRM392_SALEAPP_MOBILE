package com.example.saleapp.ui.admin.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.saleapp.data.model.response.ChatMessageDto
import com.example.saleapp.databinding.ItemChatMessageReceivedBinding
import com.example.saleapp.databinding.ItemChatMessageSentBinding
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Admin perspective: "Shop" / "Admin" senderType → SENT (right bubble)
 *                   "User" senderType              → RECEIVED (left bubble)
 */
class AdminChatMessageAdapter : ListAdapter<ChatMessageDto, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2

        fun formatTime(timestamp: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val date = inputFormat.parse(timestamp)
                date?.let { outputFormat.format(it) } ?: timestamp
            } catch (_: Exception) {
                timestamp
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).senderType == "User") VIEW_TYPE_RECEIVED else VIEW_TYPE_SENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> SentMessageViewHolder(
                ItemChatMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> ReceivedMessageViewHolder(
                ItemChatMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
        }
    }

    class SentMessageViewHolder(private val binding: ItemChatMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessageDto) {
            binding.tvMessage.text = message.message
            binding.tvTime.text = formatTime(message.sentAt)
        }
    }

    class ReceivedMessageViewHolder(private val binding: ItemChatMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessageDto) {
            binding.tvMessage.text = message.message
            binding.tvTime.text = formatTime(message.sentAt)
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<ChatMessageDto>() {
        override fun areItemsTheSame(oldItem: ChatMessageDto, newItem: ChatMessageDto) =
            oldItem.chatMessageId == newItem.chatMessageId

        override fun areContentsTheSame(oldItem: ChatMessageDto, newItem: ChatMessageDto) =
            oldItem == newItem
    }
}

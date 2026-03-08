package com.example.saleapp.ui.chat

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

class ChatMessageAdapter : ListAdapter<ChatMessageDto, RecyclerView.ViewHolder>(MessageDiffCallback()) {

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
        val message = getItem(position)
        return if (message.senderType == "User") VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val binding = ItemChatMessageSentBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                SentMessageViewHolder(binding)
            }
            else -> {
                val binding = ItemChatMessageReceivedBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ReceivedMessageViewHolder(binding)
            }
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
        override fun areItemsTheSame(oldItem: ChatMessageDto, newItem: ChatMessageDto): Boolean {
            return oldItem.chatMessageId == newItem.chatMessageId
        }

        override fun areContentsTheSame(oldItem: ChatMessageDto, newItem: ChatMessageDto): Boolean {
            return oldItem == newItem
        }
    }
}


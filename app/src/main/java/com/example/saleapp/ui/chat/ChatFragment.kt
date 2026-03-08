package com.example.saleapp.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saleapp.core.base.BaseFragment
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.core.utils.showToast
import com.example.saleapp.databinding.FragmentChatBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatFragment : BaseFragment<FragmentChatBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentChatBinding =
        FragmentChatBinding::inflate

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var messageAdapter: ChatMessageAdapter
    private var conversationId: Int? = null

    override fun setupViews() {
        setupRecyclerView()
        setupInputArea()

        // Connect to chat and create conversation
        viewModel.connectToChat()
        viewModel.createOrGetConversation()
    }

    private fun setupRecyclerView() {
        messageAdapter = ChatMessageAdapter()
        binding.rvMessages.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
        }
    }

    private fun setupInputArea() {
        binding.fabSend.setOnClickListener {
            val message = binding.etMessage.text?.toString()?.trim()
            if (!message.isNullOrEmpty() && conversationId != null) {
                viewModel.sendMessage(conversationId!!, message)
                binding.etMessage.text?.clear()
            }
        }

        // Typing indicator
        var typingJob: kotlinx.coroutines.Job? = null
        binding.etMessage.addTextChangedListener { text ->
            typingJob?.cancel()
            if (!text.isNullOrEmpty() && conversationId != null) {
                typingJob = lifecycleScope.launch {
                    viewModel.sendTypingIndicator(conversationId!!)
                }
            }
        }
    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            // Observe conversation creation
            launch {
                viewModel.conversationState.collectLatest { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.pbLoading.visibility = View.VISIBLE
                        }
                        is UiState.Success -> {
                            binding.pbLoading.visibility = View.GONE
                            conversationId = state.data.conversationId
                            viewModel.loadMessages(state.data.conversationId)
                        }
                        is UiState.Error -> {
                            binding.pbLoading.visibility = View.GONE
                            showToast(state.message)
                        }
                        else -> {}
                    }
                }
            }

            // Observe messages
            launch {
                viewModel.messages.collectLatest { messages ->
                    messageAdapter.submitList(messages)
                    if (messages.isNotEmpty()) {
                        binding.llEmptyState.visibility = View.GONE
                        binding.rvMessages.scrollToPosition(messages.size - 1)
                    } else {
                        binding.llEmptyState.visibility = View.VISIBLE
                    }
                }
            }

            // Observe connection status
            launch {
                viewModel.connectionState.collectLatest { status ->
                    when (status) {
                        "Connected" -> {
                            binding.cvConnectionStatus.visibility = View.GONE
                        }
                        "Connecting..." -> {
                            binding.cvConnectionStatus.visibility = View.VISIBLE
                            binding.tvConnectionStatus.text = "Connecting..."
                        }
                        "Disconnected" -> {
                            binding.cvConnectionStatus.visibility = View.VISIBLE
                            binding.tvConnectionStatus.text = "Disconnected. Trying to reconnect..."
                        }
                    }
                }
            }

            // Observe typing indicator
            launch {
                viewModel.isTyping.collectLatest { isTyping ->
                    binding.llTypingIndicator.visibility = if (isTyping) View.VISIBLE else View.GONE
                }
            }

            // Observe send message state
            launch {
                viewModel.sendMessageState.collectLatest { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.fabSend.isEnabled = false
                        }
                        is UiState.Success -> {
                            binding.fabSend.isEnabled = true
                        }
                        is UiState.Error -> {
                            binding.fabSend.isEnabled = true
                            showToast(getString(com.example.saleapp.R.string.chat_send_failed, state.message))
                        }
                        else -> {
                            binding.fabSend.isEnabled = true
                        }
                    }
                }
            }

            // Observe new messages
            launch {
                viewModel.newMessage.collectLatest { message ->
                    message?.let {
                        // Scroll to bottom when new message arrives
                        binding.rvMessages.scrollToPosition(messageAdapter.itemCount - 1)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.disconnectFromChat()
    }
}


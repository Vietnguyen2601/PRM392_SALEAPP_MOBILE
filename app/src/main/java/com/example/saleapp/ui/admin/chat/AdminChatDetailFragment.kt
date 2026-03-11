package com.example.saleapp.ui.admin.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saleapp.core.base.BaseFragment
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.databinding.FragmentAdminChatDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminChatDetailFragment : BaseFragment<FragmentAdminChatDetailBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAdminChatDetailBinding =
        FragmentAdminChatDetailBinding::inflate

    private val viewModel: AdminChatDetailViewModel by viewModels()
    private lateinit var messageAdapter: AdminChatMessageAdapter
    private var conversationId: Int = 0

    override fun setupViews() {
        conversationId = arguments?.getInt("conversationId") ?: 0
        val username = arguments?.getString("username") ?: "User"

        binding.tvTitle.text = username
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        // "Close conversation" menu option via long-press on toolbar title
        binding.toolbar.setOnLongClickListener {
            showCloseConversationDialog()
            true
        }

        messageAdapter = AdminChatMessageAdapter()
        binding.rvMessages.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
        }

        binding.fabSend.setOnClickListener {
            val message = binding.etMessage.text?.toString()?.trim()
            if (!message.isNullOrEmpty()) {
                viewModel.sendMessage(conversationId, message)
                binding.etMessage.text?.clear()
            }
        }

        var typingJob: kotlinx.coroutines.Job? = null
        binding.etMessage.addTextChangedListener { text ->
            typingJob?.cancel()
            if (!text.isNullOrEmpty()) {
                typingJob = lifecycleScope.launch {
                    viewModel.sendTypingIndicator(conversationId)
                }
            }
        }

        viewModel.connectToChat()
        viewModel.loadMessages(conversationId)
    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.connectionState.collectLatest { status ->
                        when (status) {
                            "Connected" -> binding.cvConnectionStatus.visibility = View.GONE
                            else -> {
                                binding.cvConnectionStatus.visibility = View.VISIBLE
                                binding.tvConnectionStatus.text = status
                            }
                        }
                    }
                }

                launch {
                    viewModel.messages.collectLatest { messages ->
                        messageAdapter.submitList(messages.toList())
                        if (messages.isNotEmpty()) {
                            binding.rvMessages.post {
                                binding.rvMessages.scrollToPosition(messages.size - 1)
                            }
                        }
                    }
                }

                launch {
                    viewModel.isTyping.collectLatest { typing ->
                        binding.llTypingIndicator.visibility = if (typing) View.VISIBLE else View.GONE
                    }
                }

                launch {
                    viewModel.sendMessageState.collectLatest { state ->
                        binding.fabSend.isEnabled = state !is UiState.Loading
                    }
                }
            }
        }
    }

    private fun showCloseConversationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Đóng cuộc trò chuyện")
            .setMessage("Bạn có chắc muốn đóng cuộc trò chuyện này không?")
            .setPositiveButton("Đóng") { _, _ ->
                viewModel.closeConversation(conversationId) {
                    findNavController().popBackStack()
                }
            }
            .setNegativeButton("Huỷ", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.disconnectFromChat()
    }
}

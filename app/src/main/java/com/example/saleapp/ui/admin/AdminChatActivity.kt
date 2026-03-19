package com.example.saleapp.ui.admin

import android.content.Intent
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saleapp.R
import com.example.saleapp.core.base.BaseActivity
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.core.utils.showToast
import com.example.saleapp.databinding.ActivityAdminChatBinding
import com.example.saleapp.ui.auth.login.LoginActivity
import com.example.saleapp.ui.chat.ChatMessageAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminChatActivity : BaseActivity<ActivityAdminChatBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityAdminChatBinding =
        ActivityAdminChatBinding::inflate

    private val viewModel: AdminChatViewModel by viewModels()
    private lateinit var conversationAdapter: AdminConversationAdapter
    private lateinit var messageAdapter: ChatMessageAdapter

    override fun setupViews() {
        setupToolbar()
        setupLists()
        setupInput()

        viewModel.connectToChat()
        viewModel.loadConversations()
    }

    private fun setupToolbar() {
        binding.toolbarAdmin.title = getString(R.string.admin_chat_title)
        binding.toolbarAdmin.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel)
        binding.toolbarAdmin.setNavigationOnClickListener {
            viewModel.logout()
        }
        binding.toolbarAdmin.inflateMenu(R.menu.admin_chat_menu)
        binding.toolbarAdmin.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_refresh -> {
                    viewModel.loadConversations()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupLists() {
        conversationAdapter = AdminConversationAdapter { conversation ->
            conversationAdapter.setSelectedConversation(conversation.conversationId)
            binding.llAdminEmptyState.isVisible = false
            viewModel.selectConversation(conversation)
        }
        binding.rvConversations.apply {
            adapter = conversationAdapter
            layoutManager = LinearLayoutManager(this@AdminChatActivity)
        }

        messageAdapter = ChatMessageAdapter(selfSenderType = "Shop")
        binding.rvAdminMessages.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(this@AdminChatActivity).apply {
                stackFromEnd = true
            }
        }
    }

    private fun setupInput() {
        binding.fabAdminSend.setOnClickListener {
            val message = binding.etAdminMessage.text?.toString()?.trim().orEmpty()
            val conversationId = viewModel.selectedConversation.value?.conversationId
            if (conversationId != null && message.isNotBlank()) {
                viewModel.sendMessage(conversationId, message)
                binding.etAdminMessage.text?.clear()
            } else {
                showToast(getString(R.string.admin_chat_select_conversation))
            }
        }
    }

    override fun observeData() {
        lifecycleScope.launch {
            viewModel.conversationsState.collectLatest { state ->
                when (state) {
                    is UiState.Loading -> binding.pbAdminLoading.isVisible = true
                    is UiState.Success -> {
                        binding.pbAdminLoading.isVisible = false
                        conversationAdapter.submitList(state.data)
                        val currentSelected = viewModel.selectedConversation.value
                        binding.llAdminEmptyState.isVisible = state.data.isEmpty()
                        if (currentSelected == null && state.data.isNotEmpty()) {
                            val first = state.data.first()
                            conversationAdapter.setSelectedConversation(first.conversationId)
                            binding.llAdminEmptyState.isVisible = false
                            viewModel.selectConversation(first)
                        } else {
                            conversationAdapter.setSelectedConversation(currentSelected?.conversationId)
                        }
                    }
                    is UiState.Error -> {
                        binding.pbAdminLoading.isVisible = false
                        showToast(state.message)
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launch {
            viewModel.messages.collectLatest { messages ->
                messageAdapter.submitList(messages.toList())
                if (messages.isNotEmpty()) {
                    binding.llAdminEmptyState.isVisible = false
                    binding.rvAdminMessages.post {
                        binding.rvAdminMessages.scrollToPosition(messages.size - 1)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.selectedConversation.collectLatest { conversation ->
                binding.tvSelectedConversation.text = conversation?.username ?: getString(R.string.admin_chat_select_conversation)
            }
        }

        lifecycleScope.launch {
            viewModel.connectionState.collectLatest { status ->
                when (status) {
                    "Connected" -> binding.cvConnectionStatusAdmin.isVisible = false
                    "Connecting..." -> {
                        binding.cvConnectionStatusAdmin.isVisible = true
                        binding.tvConnectionStatusAdmin.text = getString(R.string.chat_connecting)
                    }
                    else -> {
                        binding.cvConnectionStatusAdmin.isVisible = true
                        binding.tvConnectionStatusAdmin.text = getString(R.string.chat_disconnected)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isUserTypingConversation.collectLatest { conversationId ->
                binding.tvUserTyping.isVisible = conversationId != null && conversationId == viewModel.selectedConversation.value?.conversationId
            }
        }

        lifecycleScope.launch {
            viewModel.sendMessageState.collectLatest { state ->
                binding.fabAdminSend.isEnabled = state !is UiState.Loading
            }
        }

        lifecycleScope.launch {
            viewModel.logoutState.collectLatest { state ->
                when (state) {
                    is UiState.Success -> {
                        startActivity(Intent(this@AdminChatActivity, LoginActivity::class.java))
                        finishAffinity()
                    }
                    is UiState.Error -> showToast(state.message)
                    else -> Unit
                }
            }
        }

        lifecycleScope.launch {
            viewModel.error.collectLatest { message ->
                if (!message.isNullOrBlank()) showToast(message)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.disconnectFromChat()
    }
}

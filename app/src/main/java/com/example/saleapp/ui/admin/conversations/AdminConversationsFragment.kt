package com.example.saleapp.ui.admin.conversations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saleapp.R
import com.example.saleapp.core.base.BaseFragment
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.core.utils.gone
import com.example.saleapp.core.utils.visible
import com.example.saleapp.databinding.FragmentAdminConversationsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminConversationsFragment : BaseFragment<FragmentAdminConversationsBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAdminConversationsBinding =
        FragmentAdminConversationsBinding::inflate

    private val viewModel: AdminConversationsViewModel by viewModels()
    private lateinit var adapter: ConversationItemAdapter

    override fun setupViews() {
        adapter = ConversationItemAdapter { conversation ->
            val bundle = Bundle().apply {
                putInt("conversationId", conversation.conversationId)
                putString("username", conversation.username)
            }
            findNavController().navigate(
                R.id.action_adminConversationsFragment_to_adminChatDetailFragment,
                bundle
            )
        }

        binding.rvConversations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AdminConversationsFragment.adapter
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadConversations()
        }

        viewModel.connectToSignalR()
        viewModel.loadConversations()
    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.conversationsState.collectLatest { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.progressBar.visible()
                            binding.tvEmpty.gone()
                        }
                        is UiState.Success -> {
                            binding.swipeRefresh.isRefreshing = false
                            binding.progressBar.gone()
                            if (state.data.isEmpty()) {
                                binding.rvConversations.gone()
                                binding.tvEmpty.visible()
                            } else {
                                binding.rvConversations.visible()
                                binding.tvEmpty.gone()
                                adapter.submitList(state.data)
                            }
                        }
                        is UiState.Error -> {
                            binding.swipeRefresh.isRefreshing = false
                            binding.progressBar.gone()
                            binding.tvEmpty.visible()
                            binding.tvEmpty.text = state.message
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}

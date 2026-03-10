package com.example.saleapp.ui.home
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.saleapp.core.base.BaseFragment
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.core.utils.showToast
import com.example.saleapp.databinding.FragmentHomeBinding
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHomeBinding =
        FragmentHomeBinding::inflate

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter

    override fun setupViews() {
        setupRecyclerView()
        setupSearch()
        setupFilterButton()
        viewModel.loadProducts()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter { product ->
            // Navigate to product detail
            val productId = product.productId ?: product.id ?: return@ProductAdapter
            val action = HomeFragmentDirections.actionHomeFragmentToProductDetailFragment(productId)
            findNavController().navigate(action)
        }

        binding.rvProducts.apply {
            adapter = productAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateSearchQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFilterButton() {
        binding.btnFilter.setOnClickListener {
            val sheet = FilterBottomSheetFragment(
                currentFilter = viewModel.filterState.value
            ) { newFilter ->
                viewModel.applyFilter(newFilter)
            }
            sheet.show(childFragmentManager, "FilterBottomSheet")
        }
    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.productsState.collectLatest { state ->
                        when (state) {
                            is UiState.Loading -> showLoading(true)
                            is UiState.Success -> {
                                showLoading(false)
                                productAdapter.updateProducts(state.data)
                                updateResultCount(state.data.size, viewModel.filterState.value)
                                showEmptyState(state.data.isEmpty())
                            }
                            is UiState.Error -> {
                                showLoading(false)
                                showToast(state.message)
                            }
                            else -> showLoading(false)
                        }
                    }
                }

                launch {
                    viewModel.filterState.collectLatest { filter ->
                        updateActiveFilterChips(filter)
                        val tint = if (filter.isActive()) {
                            requireContext().getColor(com.example.saleapp.R.color.teal_700)
                        } else {
                            requireContext().getColor(com.example.saleapp.R.color.purple_500)
                        }
                        binding.btnFilter.backgroundTintList =
                            android.content.res.ColorStateList.valueOf(tint)
                    }
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyState(show: Boolean) {
        binding.layoutEmpty.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvProducts.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun updateResultCount(count: Int, filter: FilterState) {
        val label = buildString {
            if (filter.isActive()) {
                append("$count result(s) found")
            } else {
                append("All Products ($count)")
            }
        }
        binding.tvResultCount.text = label
    }

    private fun updateActiveFilterChips(filter: FilterState) {
        val chipGroup = binding.chipGroupActiveFilters
        chipGroup.removeAllViews()

        val activeLabels = mutableListOf<Pair<String, () -> Unit>>()

        if (filter.sortOption != SortOption.DEFAULT) {
            activeLabels.add(filter.sortOption.label to {
                viewModel.applyFilter(filter.copy(sortOption = SortOption.DEFAULT))
            })
        }
        if (filter.category != null) {
            activeLabels.add(filter.category to {
                viewModel.applyFilter(filter.copy(category = null))
            })
        }
        if (filter.minRating > 0f) {
            activeLabels.add("★ ${filter.minRating.toInt()}+" to {
                viewModel.applyFilter(filter.copy(minRating = 0f))
            })
        }
        if (filter.minPrice > 0f || filter.maxPrice < 1000f) {
            val maxLabel = if (filter.maxPrice >= 1000f) "1000+" else "${filter.maxPrice.toInt()}"
            activeLabels.add("$${filter.minPrice.toInt()}-$$maxLabel" to {
                viewModel.applyFilter(filter.copy(minPrice = 0f, maxPrice = 1000f))
            })
        }

        if (activeLabels.isEmpty()) {
            binding.hsvActiveFilters.visibility = View.GONE
            return
        }

        binding.hsvActiveFilters.visibility = View.VISIBLE
        activeLabels.forEach { (label, onClose) ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCloseIconVisible = true
                setOnCloseIconClickListener { onClose() }
            }
            chipGroup.addView(chip)
        }
    }
}

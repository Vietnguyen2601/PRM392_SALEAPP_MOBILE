package com.example.saleapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.saleapp.databinding.BottomSheetFilterBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FilterBottomSheetFragment(
    private val currentFilter: FilterState,
    private val onApply: (FilterState) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreCurrentFilter()
        setupListeners()
    }

    private fun restoreCurrentFilter() {
        binding.apply {
            // Restore sort selection
            when (currentFilter.sortOption) {
                SortOption.DEFAULT -> chipSortDefault.isChecked = true
                SortOption.PRICE_ASC -> chipSortPriceAsc.isChecked = true
                SortOption.PRICE_DESC -> chipSortPriceDesc.isChecked = true
                SortOption.RATING -> chipSortRating.isChecked = true
                SortOption.POPULAR -> chipSortPopular.isChecked = true
            }

            // Restore category
            when (currentFilter.category?.lowercase()) {
                null -> chipCatAll.isChecked = true
                "electronics" -> chipCatElectronics.isChecked = true
                "fashion" -> chipCatFashion.isChecked = true
                "food & beverage" -> chipCatFood.isChecked = true
                else -> chipCatOther.isChecked = true
            }

            // Restore rating
            when {
                currentFilter.minRating >= 5f -> chipRating5.isChecked = true
                currentFilter.minRating >= 4f -> chipRating4.isChecked = true
                currentFilter.minRating >= 3f -> chipRating3.isChecked = true
                else -> chipRatingAll.isChecked = true
            }

            // Restore price range
            sliderPrice.setValues(currentFilter.minPrice, currentFilter.maxPrice)
            updatePriceLabel(currentFilter.minPrice, currentFilter.maxPrice)
        }
    }

    private fun setupListeners() {
        binding.apply {
            // Price slider change
            sliderPrice.addOnChangeListener { slider, _, _ ->
                val values = slider.values
                updatePriceLabel(values[0], values[1])
            }

            // Reset button
            btnReset.setOnClickListener {
                chipSortDefault.isChecked = true
                chipCatAll.isChecked = true
                chipRatingAll.isChecked = true
                sliderPrice.setValues(0f, 1000f)
                updatePriceLabel(0f, 1000f)
            }

            // Apply button
            btnApply.setOnClickListener {
                val sortOption = when (chipGroupSort.checkedChipId) {
                    chipSortPriceAsc.id -> SortOption.PRICE_ASC
                    chipSortPriceDesc.id -> SortOption.PRICE_DESC
                    chipSortRating.id -> SortOption.RATING
                    chipSortPopular.id -> SortOption.POPULAR
                    else -> SortOption.DEFAULT
                }

                val category = when (chipGroupCategory.checkedChipId) {
                    chipCatElectronics.id -> "Electronics"
                    chipCatFashion.id -> "Fashion"
                    chipCatFood.id -> "Food & Beverage"
                    chipCatOther.id -> "Other"
                    else -> null
                }

                val minRating = when (chipGroupRating.checkedChipId) {
                    chipRating3.id -> 3f
                    chipRating4.id -> 4f
                    chipRating5.id -> 5f
                    else -> 0f
                }

                val priceValues = sliderPrice.values
                val newFilter = currentFilter.copy(
                    sortOption = sortOption,
                    category = category,
                    minRating = minRating,
                    minPrice = priceValues[0],
                    maxPrice = priceValues[1]
                )
                onApply(newFilter)
                dismiss()
            }
        }
    }

    private fun updatePriceLabel(min: Float, max: Float) {
        val maxLabel = if (max >= 1000f) "$1000+" else "$${"%.0f".format(max)}"
        binding.tvPriceRange.text = "$${"%.0f".format(min)} — $maxLabel"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


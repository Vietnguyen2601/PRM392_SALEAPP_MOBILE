package com.example.saleapp.ui.home

data class FilterState(
    val sortOption: SortOption = SortOption.DEFAULT,
    val category: String? = null,          // null = All
    val minRating: Float = 0f,
    val minPrice: Float = 0f,
    val maxPrice: Float = 1000f,
    val searchQuery: String = ""
) {
    fun isActive(): Boolean =
        sortOption != SortOption.DEFAULT ||
                category != null ||
                minRating > 0f ||
                minPrice > 0f ||
                maxPrice < 1000f ||
                searchQuery.isNotBlank()
}

enum class SortOption(val label: String) {
    DEFAULT("Default"),
    PRICE_ASC("Price: Low to High"),
    PRICE_DESC("Price: High to Low"),
    RATING("Top Rated"),
    POPULAR("Most Popular")
}


package com.nikunjagarwala.dummyshop.data.repository

enum class SortField(val apiValue: String?, val label: String) {
    RELEVANCE(null, "Relevance"),
    TITLE("title", "Name"),
    PRICE("price", "Price"),
    RATING("rating", "Rating")
}

enum class SortOrder(val apiValue: String, val label: String) {
    ASC("asc", "Ascending"),
    DESC("desc", "Descending")
}

sealed class FilterMode {
    data object All : FilterMode()
    data class Search(val query: String) : FilterMode()
    data class Category(val slug: String, val name: String) : FilterMode()
}

data class ProductQuery(
    val filter: FilterMode = FilterMode.All,
    val sortField: SortField = SortField.RELEVANCE,
    val sortOrder: SortOrder = SortOrder.ASC
) {
    /** Stable string key identifying this exact search/filter/sort combination, used as the cache scope. */
    val key: String
        get() {
            val filterPart = when (filter) {
                is FilterMode.All -> "all"
                is FilterMode.Search -> "search:${filter.query.trim().lowercase()}"
                is FilterMode.Category -> "category:${filter.slug}"
            }
            return "$filterPart|sort:${sortField.name}|order:${sortOrder.name}"
        }
}

package com.nikunjagarwala.dummyshop.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.nikunjagarwala.dummyshop.data.local.entity.CategoryEntity
import com.nikunjagarwala.dummyshop.data.repository.SortField
import com.nikunjagarwala.dummyshop.data.repository.SortOrder
import com.nikunjagarwala.dummyshop.ui.components.CachedDataBanner
import com.nikunjagarwala.dummyshop.ui.components.EmptyMessageState
import com.nikunjagarwala.dummyshop.ui.components.EmptyRetryState
import com.nikunjagarwala.dummyshop.ui.components.ProductListItem

private data class SortOption(val label: String, val field: SortField, val order: SortOrder)

private val SORT_OPTIONS = listOf(
    SortOption("Relevance", SortField.RELEVANCE, SortOrder.ASC),
    SortOption("Price: Low to High", SortField.PRICE, SortOrder.ASC),
    SortOption("Price: High to Low", SortField.PRICE, SortOrder.DESC),
    SortOption("Rating: High to Low", SortField.RATING, SortOrder.DESC),
    SortOption("Name: A to Z", SortField.TITLE, SortOrder.ASC),
    SortOption("Name: Z to A", SortField.TITLE, SortOrder.DESC)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(viewModel: ProductListViewModel, onProductClick: (Int) -> Unit) {
    val filterState by viewModel.uiFilterState.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val pagingItems = viewModel.pagingDataFlow.collectAsLazyPagingItems()

    val refreshState = pagingItems.loadState.refresh
    val appendState = pagingItems.loadState.append
    val hasCachedError = refreshState is LoadState.Error || appendState is LoadState.Error
    val isEmpty = pagingItems.itemCount == 0

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = filterState.searchText,
            onValueChange = viewModel::onSearchTextChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search products") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filterState.selectedCategory == null,
                        onClick = { viewModel.onCategorySelected(null) },
                        label = { Text("All") }
                    )
                }
                items(categories, key = { it.slug }) { category ->
                    FilterChip(
                        selected = filterState.selectedCategory?.slug == category.slug,
                        onClick = { viewModel.onCategorySelected(category) },
                        label = { Text(category.name.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            var sortMenuOpen by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { sortMenuOpen = true }) {
                    Icon(Icons.Filled.Sort, contentDescription = "Sort")
                }
                DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                    SORT_OPTIONS.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                viewModel.onSortChange(option.field, option.order)
                                sortMenuOpen = false
                            }
                        )
                    }
                }
            }
        }

        if (hasCachedError && !isEmpty) {
            CachedDataBanner(onRetry = { pagingItems.retry() })
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isEmpty && refreshState is LoadState.Error -> {
                    EmptyRetryState(onRetry = { pagingItems.retry() })
                }
                isEmpty && refreshState is LoadState.NotLoading -> {
                    EmptyMessageState(message = "No products found.")
                }
                isEmpty && refreshState is LoadState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    PullToRefreshBox(
                        isRefreshing = refreshState is LoadState.Loading,
                        onRefresh = { pagingItems.refresh() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(count = pagingItems.itemCount, key = pagingItems.itemKey { it.id }) { index ->
                                val product = pagingItems[index]
                                if (product != null) {
                                    ProductListItem(product = product, onClick = { onProductClick(product.id) })
                                }
                            }
                            if (appendState is LoadState.Loading) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

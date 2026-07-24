package com.nikunjagarwala.dummyshop.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.nikunjagarwala.dummyshop.data.connectivity.ConnectivityObserver
import com.nikunjagarwala.dummyshop.data.local.entity.CategoryEntity
import com.nikunjagarwala.dummyshop.data.local.toDomain
import com.nikunjagarwala.dummyshop.data.repository.FilterMode
import com.nikunjagarwala.dummyshop.data.repository.ProductQuery
import com.nikunjagarwala.dummyshop.data.repository.ProductRepository
import com.nikunjagarwala.dummyshop.data.repository.SortField
import com.nikunjagarwala.dummyshop.data.repository.SortOrder
import com.nikunjagarwala.dummyshop.domain.Product
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ListFilterState(
    val searchText: String = "",
    val selectedCategory: CategoryEntity? = null,
    val sortField: SortField = SortField.RELEVANCE,
    val sortOrder: SortOrder = SortOrder.ASC
)

class ProductListViewModel(
    private val repository: ProductRepository,
    connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val filterState = MutableStateFlow(ListFilterState())
    val uiFilterState: StateFlow<ListFilterState> = filterState

    @OptIn(FlowPreview::class)
    private val query: StateFlow<ProductQuery> = filterState
        .map { state ->
            val filter = when {
                state.selectedCategory != null -> FilterMode.Category(state.selectedCategory.slug, state.selectedCategory.name)
                state.searchText.isNotBlank() -> FilterMode.Search(state.searchText)
                else -> FilterMode.All
            }
            ProductQuery(filter, state.sortField, state.sortOrder)
        }
        .debounce(300)
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductQuery())

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingDataFlow: Flow<PagingData<Product>> = query
        .flatMapLatest { repository.pagingFlow(it) }
        .map { pagingData -> pagingData.map { it.toDomain() } }
        .cachedIn(viewModelScope)

    val categories: StateFlow<List<CategoryEntity>> = repository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val isOffline: StateFlow<Boolean> = connectivityObserver.isOnline
        .map { !it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), !connectivityObserver.isCurrentlyOnline())

    init {
        viewModelScope.launch { repository.refreshCategories() }
    }

    fun onSearchTextChange(text: String) {
        filterState.update { it.copy(searchText = text, selectedCategory = null) }
    }

    fun onCategorySelected(category: CategoryEntity?) {
        filterState.update { it.copy(selectedCategory = category, searchText = "") }
    }

    fun onSortChange(field: SortField, order: SortOrder) {
        filterState.update { it.copy(sortField = field, sortOrder = order) }
    }
}

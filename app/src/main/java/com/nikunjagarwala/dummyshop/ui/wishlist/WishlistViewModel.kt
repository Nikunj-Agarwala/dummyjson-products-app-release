package com.nikunjagarwala.dummyshop.ui.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikunjagarwala.dummyshop.data.local.toDomain
import com.nikunjagarwala.dummyshop.data.repository.ProductRepository
import com.nikunjagarwala.dummyshop.domain.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WishlistEntry(val product: Product, val addedAt: Long)

class WishlistViewModel(private val repository: ProductRepository) : ViewModel() {

    val items: StateFlow<List<WishlistEntry>> = repository.observeWishlist()
        .map { list -> list.map { WishlistEntry(it.product.toDomain(), it.addedAt) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _hasError = MutableStateFlow(false)
    val hasError: StateFlow<Boolean> = _hasError

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val ids = repository.getWishlistProductIds()
            _hasError.value = !repository.refreshProducts(ids)
            _isRefreshing.value = false
        }
    }

    fun remove(productId: Int) {
        viewModelScope.launch { repository.toggleWishlist(productId, currentlyWishlisted = true) }
    }
}

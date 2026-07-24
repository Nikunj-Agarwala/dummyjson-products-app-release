package com.nikunjagarwala.dummyshop.ui.detail

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

class ProductDetailViewModel(
    private val productId: Int,
    private val repository: ProductRepository
) : ViewModel() {

    val product: StateFlow<Product?> = repository.observeProduct(productId)
        .map { it?.toDomain() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val isWishlisted: StateFlow<Boolean> = repository.observeIsWishlisted(productId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val isInCart: StateFlow<Boolean> = repository.observeIsInCart(productId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _isRefreshing = MutableStateFlow(true)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _refreshFailed = MutableStateFlow(false)
    val refreshFailed: StateFlow<Boolean> = _refreshFailed

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val result = repository.refreshProduct(productId)
            _refreshFailed.value = result.isFailure
            _isRefreshing.value = false
        }
    }

    fun toggleWishlist() {
        viewModelScope.launch { repository.toggleWishlist(productId, isWishlisted.value) }
    }

    fun addToCart() {
        viewModelScope.launch { repository.addToCart(productId) }
    }
}

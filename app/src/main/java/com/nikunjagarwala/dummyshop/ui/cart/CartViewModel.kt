package com.nikunjagarwala.dummyshop.ui.cart

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

data class CartEntry(val product: Product, val quantity: Int, val addedAt: Long)

class CartViewModel(private val repository: ProductRepository) : ViewModel() {

    val items: StateFlow<List<CartEntry>> = repository.observeCart()
        .map { list -> list.map { CartEntry(it.product.toDomain(), it.quantity, it.addedAt) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val subtotal: StateFlow<Double> = items
        .map { entries -> entries.sumOf { it.product.discountedPrice * it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

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
            val ids = repository.getCartProductIds()
            _hasError.value = !repository.refreshProducts(ids)
            _isRefreshing.value = false
        }
    }

    fun updateQuantity(productId: Int, quantity: Int) {
        viewModelScope.launch { repository.updateCartQuantity(productId, quantity) }
    }

    fun remove(productId: Int) {
        viewModelScope.launch { repository.removeFromCart(productId) }
    }
}

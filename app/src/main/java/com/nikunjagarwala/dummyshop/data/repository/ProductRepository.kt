package com.nikunjagarwala.dummyshop.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.nikunjagarwala.dummyshop.data.local.AppDatabase
import com.nikunjagarwala.dummyshop.data.local.entity.CartEntity
import com.nikunjagarwala.dummyshop.data.local.entity.CartItemEntity
import com.nikunjagarwala.dummyshop.data.local.entity.CategoryEntity
import com.nikunjagarwala.dummyshop.data.local.entity.ProductEntity
import com.nikunjagarwala.dummyshop.data.local.entity.WishlistEntity
import com.nikunjagarwala.dummyshop.data.local.entity.WishlistItemEntity
import com.nikunjagarwala.dummyshop.data.local.toEntity
import com.nikunjagarwala.dummyshop.data.remote.ProductApi
import com.nikunjagarwala.dummyshop.data.remote.dto.CategoryDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class ProductRepository(
    private val api: ProductApi,
    private val db: AppDatabase
) {
    companion object {
        const val PAGE_SIZE = 20
        private const val REFETCH_CONCURRENCY = 4
    }

    @OptIn(ExperimentalPagingApi::class)
    fun pagingFlow(query: ProductQuery): Flow<PagingData<ProductEntity>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = PAGE_SIZE, enablePlaceholders = false),
            remoteMediator = ProductsRemoteMediator(query, api, db, PAGE_SIZE),
            pagingSourceFactory = { db.productDao().pagingSource(query.key) }
        ).flow

    fun observeProduct(id: Int): Flow<ProductEntity?> = db.productDao().observeById(id)

    suspend fun refreshProduct(id: Int): Result<Unit> = runCatching {
        val dto = api.getProduct(id)
        db.productDao().upsert(dto.toEntity(System.currentTimeMillis()))
    }

    fun observeCategories(): Flow<List<CategoryEntity>> = db.categoryDao().observeAll()

    suspend fun refreshCategories(): Result<Unit> = runCatching {
        val categories = api.getCategories().map(CategoryDto::toEntity)
        db.categoryDao().replaceAll(categories)
    }

    fun observeWishlist(): Flow<List<WishlistItemEntity>> = db.wishlistDao().observeWishlist()

    suspend fun getWishlistProductIds(): List<Int> = db.wishlistDao().getAllProductIds()

    suspend fun getCartProductIds(): List<Int> = db.cartDao().getAllProductIds()

    fun observeIsWishlisted(productId: Int): Flow<Boolean> = db.wishlistDao().observeIsWishlisted(productId)

    suspend fun toggleWishlist(productId: Int, currentlyWishlisted: Boolean) {
        if (currentlyWishlisted) {
            db.wishlistDao().remove(productId)
        } else {
            db.wishlistDao().add(WishlistEntity(productId, System.currentTimeMillis()))
        }
    }

    fun observeCart(): Flow<List<CartItemEntity>> = db.cartDao().observeCart()

    fun observeIsInCart(productId: Int): Flow<Boolean> = db.cartDao().observeIsInCart(productId)

    suspend fun addToCart(productId: Int) {
        val existing = db.cartDao().get(productId)
        db.cartDao().upsert(
            CartEntity(
                productId = productId,
                quantity = (existing?.quantity ?: 0) + 1,
                addedAt = existing?.addedAt ?: System.currentTimeMillis()
            )
        )
    }

    suspend fun updateCartQuantity(productId: Int, quantity: Int) {
        if (quantity <= 0) {
            db.cartDao().remove(productId)
            return
        }
        val existing = db.cartDao().get(productId)
        db.cartDao().upsert(CartEntity(productId, quantity, existing?.addedAt ?: System.currentTimeMillis()))
    }

    suspend fun removeFromCart(productId: Int) = db.cartDao().remove(productId)

    /** Best-effort background refresh of already-cached products (used by Wishlist/Cart on open). */
    suspend fun refreshProducts(ids: List<Int>): Boolean = coroutineScope {
        if (ids.isEmpty()) return@coroutineScope true
        val semaphore = Semaphore(REFETCH_CONCURRENCY)
        val results = ids.map { id ->
            async(Dispatchers.IO) {
                semaphore.withPermit { refreshProduct(id).isSuccess }
            }
        }
        results.awaitAll().all { it }
    }
}

private fun CategoryDto.toEntity() = CategoryEntity(slug = slug, name = name)

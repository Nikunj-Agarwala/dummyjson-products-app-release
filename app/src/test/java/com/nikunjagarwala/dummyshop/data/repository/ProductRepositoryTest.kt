package com.nikunjagarwala.dummyshop.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nikunjagarwala.dummyshop.data.local.AppDatabase
import com.nikunjagarwala.dummyshop.data.local.toEntity
import com.nikunjagarwala.dummyshop.testutil.FakeProductApi
import com.nikunjagarwala.dummyshop.testutil.sampleProductDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProductRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: ProductRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ProductRepository(FakeProductApi(), db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun seedProduct(id: Int) {
        db.productDao().upsert(sampleProductDto(id).toEntity(cachedAt = 0L))
    }

    @Test
    fun toggleWishlist_addsThenRemoves() = runTest {
        seedProduct(1)

        repository.toggleWishlist(1, currentlyWishlisted = false)
        assertEquals(listOf(1), repository.getWishlistProductIds())
        assertTrue(repository.observeWishlist().first().any { it.product.id == 1 })

        repository.toggleWishlist(1, currentlyWishlisted = true)
        assertEquals(emptyList<Int>(), repository.getWishlistProductIds())
    }

    @Test
    fun addToCart_incrementsQuantity_andRemovesAtZero() = runTest {
        seedProduct(1)

        repository.addToCart(1)
        repository.addToCart(1)
        val afterTwoAdds = repository.observeCart().first().first { it.product.id == 1 }
        assertEquals(2, afterTwoAdds.quantity)

        repository.updateCartQuantity(1, 0)
        assertEquals(emptyList<Int>(), repository.getCartProductIds())
    }

    @Test
    fun refreshProducts_updatesCachedCopyFromNetwork() = runTest {
        seedProduct(1)
        val api = object : FakeProductApi() {
            override suspend fun getProduct(id: Int) = sampleProductDto(id, title = "Refreshed Title")
        }
        val repoWithApi = ProductRepository(api, db)

        val ok = repoWithApi.refreshProducts(listOf(1))

        assertTrue(ok)
        val stored = db.productDao().observeById(1).first()
        assertEquals("Refreshed Title", stored?.title)
    }
}

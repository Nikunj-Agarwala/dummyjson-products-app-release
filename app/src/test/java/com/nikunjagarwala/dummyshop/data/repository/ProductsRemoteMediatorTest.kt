package com.nikunjagarwala.dummyshop.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nikunjagarwala.dummyshop.data.local.AppDatabase
import com.nikunjagarwala.dummyshop.data.local.entity.ProductEntity
import com.nikunjagarwala.dummyshop.data.remote.dto.ProductListResponseDto
import com.nikunjagarwala.dummyshop.testutil.FakeProductApi
import com.nikunjagarwala.dummyshop.testutil.sampleProductDto
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProductsRemoteMediatorTest {

    private lateinit var db: AppDatabase
    private val pageSize = 5
    private val query = ProductQuery(filter = FilterMode.All)

    private val emptyPagingState = PagingState<Int, ProductEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = pageSize),
        leadingPlaceholderCount = 0
    )

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun cachedTitles(): List<String> {
        val page = db.productDao().pagingSource(query.key)
            .load(PagingSource.LoadParams.Refresh(key = null, loadSize = 50, placeholdersEnabled = false))
        check(page is PagingSource.LoadResult.Page)
        return page.data.map { it.title }
    }

    @Test
    fun refresh_cachesFirstPage_andReachesEndWhenFewerThanPageSize() = runTest {
        val api = object : FakeProductApi() {
            override suspend fun getProducts(limit: Int, skip: Int, sortBy: String?, order: String?) =
                ProductListResponseDto(products = listOf(sampleProductDto(1), sampleProductDto(2)), total = 2, skip = 0, limit = limit)
        }
        val mediator = ProductsRemoteMediator(query, api, db, pageSize)

        val result = mediator.load(LoadType.REFRESH, emptyPagingState)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertEquals(listOf("Product 1", "Product 2"), cachedTitles())
        val remoteKey = db.remoteKeyDao().get(query.key)
        assertTrue(remoteKey?.endOfPaginationReached == true)
    }

    @Test
    fun append_fetchesNextSkip_andAccumulatesResults() = runTest {
        val api = object : FakeProductApi() {
            override suspend fun getProducts(limit: Int, skip: Int, sortBy: String?, order: String?): ProductListResponseDto {
                val products = if (skip == 0) {
                    (1..pageSize).map { sampleProductDto(it) }
                } else {
                    listOf(sampleProductDto(pageSize + 1))
                }
                return ProductListResponseDto(products = products, total = pageSize + 1, skip = skip, limit = limit)
            }
        }
        val mediator = ProductsRemoteMediator(query, api, db, pageSize)

        val refreshResult = mediator.load(LoadType.REFRESH, emptyPagingState)
        assertFalse((refreshResult as RemoteMediator.MediatorResult.Success).endOfPaginationReached)

        val appendResult = mediator.load(LoadType.APPEND, emptyPagingState)
        assertTrue((appendResult as RemoteMediator.MediatorResult.Success).endOfPaginationReached)

        assertEquals(pageSize + 1, cachedTitles().size)
    }

    @Test
    fun refresh_networkFailure_returnsErrorAndPreservesExistingCache() = runTest {
        val workingApi = object : FakeProductApi() {
            override suspend fun getProducts(limit: Int, skip: Int, sortBy: String?, order: String?) =
                ProductListResponseDto(products = listOf(sampleProductDto(1)), total = 1, skip = 0, limit = limit)
        }
        ProductsRemoteMediator(query, workingApi, db, pageSize).load(LoadType.REFRESH, emptyPagingState)
        assertEquals(listOf("Product 1"), cachedTitles())

        val failingApi = object : FakeProductApi() {
            override suspend fun getProducts(limit: Int, skip: Int, sortBy: String?, order: String?): ProductListResponseDto {
                throw IOException("offline")
            }
        }
        val result = ProductsRemoteMediator(query, failingApi, db, pageSize).load(LoadType.REFRESH, emptyPagingState)

        assertTrue(result is RemoteMediator.MediatorResult.Error)
        assertEquals(listOf("Product 1"), cachedTitles())
    }
}

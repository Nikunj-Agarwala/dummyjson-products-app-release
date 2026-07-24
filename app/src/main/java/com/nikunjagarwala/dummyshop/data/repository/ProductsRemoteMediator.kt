package com.nikunjagarwala.dummyshop.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.nikunjagarwala.dummyshop.data.local.AppDatabase
import com.nikunjagarwala.dummyshop.data.local.entity.ListEntryEntity
import com.nikunjagarwala.dummyshop.data.local.entity.ProductEntity
import com.nikunjagarwala.dummyshop.data.local.entity.RemoteKeyEntity
import com.nikunjagarwala.dummyshop.data.local.toEntity
import com.nikunjagarwala.dummyshop.data.remote.ProductApi
import retrofit2.HttpException
import java.io.IOException

/**
 * Fetches one page for [query] from DummyJSON and merges it into Room, scoped by [ProductQuery.key].
 * On failure this returns [MediatorResult.Error] without touching Room, so whatever is already
 * cached for this query keeps showing on screen while Paging3 surfaces the error via LoadState.
 */
@OptIn(ExperimentalPagingApi::class)
class ProductsRemoteMediator(
    private val query: ProductQuery,
    private val api: ProductApi,
    private val db: AppDatabase,
    private val pageSize: Int
) : RemoteMediator<Int, ProductEntity>() {

    override suspend fun initialize(): InitializeAction = InitializeAction.LAUNCH_INITIAL_REFRESH

    override suspend fun load(loadType: LoadType, state: PagingState<Int, ProductEntity>): MediatorResult {
        return try {
            val skip = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKey = db.remoteKeyDao().get(query.key)
                    if (remoteKey?.endOfPaginationReached == true) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }
                    remoteKey?.nextSkip ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
            }

            val response = when (val filter = query.filter) {
                is FilterMode.All -> api.getProducts(pageSize, skip, query.sortField.apiValue, query.sortOrder.apiValue)
                is FilterMode.Search -> api.searchProducts(
                    filter.query, pageSize, skip, query.sortField.apiValue, query.sortOrder.apiValue
                )
                is FilterMode.Category -> api.getProductsByCategory(
                    filter.slug, pageSize, skip, query.sortField.apiValue, query.sortOrder.apiValue
                )
            }

            val now = System.currentTimeMillis()
            val entities = response.products.map { it.toEntity(now) }
            val entries = response.products.mapIndexed { index, dto ->
                ListEntryEntity(queryKey = query.key, position = skip + index, productId = dto.id)
            }
            val endReached = response.products.isEmpty() || skip + response.products.size >= response.total

            db.productDao().refreshPage(
                queryKey = query.key,
                clearExisting = loadType == LoadType.REFRESH,
                entries = entries,
                products = entities
            )
            db.remoteKeyDao().upsert(
                RemoteKeyEntity(
                    queryKey = query.key,
                    nextSkip = if (endReached) null else skip + response.products.size,
                    endOfPaginationReached = endReached
                )
            )

            MediatorResult.Success(endOfPaginationReached = endReached)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}

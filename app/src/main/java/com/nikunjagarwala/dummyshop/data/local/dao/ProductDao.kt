package com.nikunjagarwala.dummyshop.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.nikunjagarwala.dummyshop.data.local.entity.ListEntryEntity
import com.nikunjagarwala.dummyshop.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query(
        """
        SELECT p.* FROM products p
        INNER JOIN list_entries e ON e.productId = p.id
        WHERE e.queryKey = :queryKey
        ORDER BY e.position ASC
        """
    )
    fun pagingSource(queryKey: String): PagingSource<Int, ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertListEntries(entries: List<ListEntryEntity>)

    @Query("DELETE FROM list_entries WHERE queryKey = :queryKey")
    suspend fun clearListEntries(queryKey: String)

    @Query("SELECT * FROM products WHERE id = :id")
    fun observeById(id: Int): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Int): ProductEntity?

    @Transaction
    suspend fun refreshPage(queryKey: String, clearExisting: Boolean, entries: List<ListEntryEntity>, products: List<ProductEntity>) {
        if (clearExisting) clearListEntries(queryKey)
        upsertAll(products)
        upsertListEntries(entries)
    }
}

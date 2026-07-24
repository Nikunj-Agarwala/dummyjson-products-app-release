package com.nikunjagarwala.dummyshop.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nikunjagarwala.dummyshop.data.local.entity.WishlistEntity
import com.nikunjagarwala.dummyshop.data.local.entity.WishlistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {
    @Query(
        """
        SELECT p.*, w.addedAt as addedAt FROM products p
        INNER JOIN wishlist w ON w.productId = p.id
        ORDER BY w.addedAt DESC
        """
    )
    fun observeWishlist(): Flow<List<WishlistItemEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist WHERE productId = :productId)")
    fun observeIsWishlisted(productId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(entity: WishlistEntity)

    @Query("DELETE FROM wishlist WHERE productId = :productId")
    suspend fun remove(productId: Int)

    @Query("SELECT productId FROM wishlist")
    suspend fun getAllProductIds(): List<Int>
}

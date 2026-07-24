package com.nikunjagarwala.dummyshop.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nikunjagarwala.dummyshop.data.local.entity.CartEntity
import com.nikunjagarwala.dummyshop.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query(
        """
        SELECT p.*, c.quantity as quantity, c.addedAt as addedAt FROM products p
        INNER JOIN cart c ON c.productId = p.id
        ORDER BY c.addedAt DESC
        """
    )
    fun observeCart(): Flow<List<CartItemEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM cart WHERE productId = :productId)")
    fun observeIsInCart(productId: Int): Flow<Boolean>

    @Query("SELECT * FROM cart WHERE productId = :productId")
    suspend fun get(productId: Int): CartEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CartEntity)

    @Query("DELETE FROM cart WHERE productId = :productId")
    suspend fun remove(productId: Int)

    @Query("SELECT productId FROM cart")
    suspend fun getAllProductIds(): List<Int>
}

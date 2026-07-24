package com.nikunjagarwala.dummyshop.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nikunjagarwala.dummyshop.data.local.dao.CartDao
import com.nikunjagarwala.dummyshop.data.local.dao.CategoryDao
import com.nikunjagarwala.dummyshop.data.local.dao.ProductDao
import com.nikunjagarwala.dummyshop.data.local.dao.RemoteKeyDao
import com.nikunjagarwala.dummyshop.data.local.dao.WishlistDao
import com.nikunjagarwala.dummyshop.data.local.entity.CartEntity
import com.nikunjagarwala.dummyshop.data.local.entity.CategoryEntity
import com.nikunjagarwala.dummyshop.data.local.entity.ListEntryEntity
import com.nikunjagarwala.dummyshop.data.local.entity.ProductEntity
import com.nikunjagarwala.dummyshop.data.local.entity.RemoteKeyEntity
import com.nikunjagarwala.dummyshop.data.local.entity.WishlistEntity

@Database(
    entities = [
        ProductEntity::class,
        ListEntryEntity::class,
        RemoteKeyEntity::class,
        WishlistEntity::class,
        CartEntity::class,
        CategoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun remoteKeyDao(): RemoteKeyDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun cartDao(): CartDao
    abstract fun categoryDao(): CategoryDao
}

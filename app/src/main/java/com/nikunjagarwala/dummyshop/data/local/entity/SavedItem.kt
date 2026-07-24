package com.nikunjagarwala.dummyshop.data.local.entity

import androidx.room.Embedded

data class WishlistItemEntity(
    @Embedded val product: ProductEntity,
    val addedAt: Long
)

data class CartItemEntity(
    @Embedded val product: ProductEntity,
    val quantity: Int,
    val addedAt: Long
)

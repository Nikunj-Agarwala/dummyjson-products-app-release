package com.nikunjagarwala.dummyshop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart")
data class CartEntity(
    @PrimaryKey val productId: Int,
    val quantity: Int,
    val addedAt: Long
)

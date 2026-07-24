package com.nikunjagarwala.dummyshop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val discountPercentage: Double,
    val rating: Double,
    val stock: Int,
    val tagsJson: String,
    val brand: String?,
    val sku: String,
    val weight: Double,
    val dimensionsJson: String,
    val warrantyInformation: String,
    val shippingInformation: String,
    val availabilityStatus: String,
    val reviewsJson: String,
    val returnPolicy: String,
    val minimumOrderQuantity: Int,
    val metaJson: String,
    val thumbnail: String,
    val imagesJson: String,
    val cachedAt: Long
)

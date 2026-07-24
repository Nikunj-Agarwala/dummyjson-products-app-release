package com.nikunjagarwala.dummyshop.domain

data class Dimensions(val width: Double, val height: Double, val depth: Double)

data class Review(
    val rating: Int,
    val comment: String,
    val date: String,
    val reviewerName: String,
    val reviewerEmail: String
)

data class ProductMeta(val createdAt: String, val updatedAt: String, val barcode: String, val qrCode: String)

data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val discountPercentage: Double,
    val rating: Double,
    val stock: Int,
    val tags: List<String>,
    val brand: String?,
    val sku: String,
    val weight: Double,
    val dimensions: Dimensions,
    val warrantyInformation: String,
    val shippingInformation: String,
    val availabilityStatus: String,
    val reviews: List<Review>,
    val returnPolicy: String,
    val minimumOrderQuantity: Int,
    val meta: ProductMeta,
    val thumbnail: String,
    val images: List<String>
) {
    val discountedPrice: Double
        get() = price * (1 - discountPercentage / 100.0)
}

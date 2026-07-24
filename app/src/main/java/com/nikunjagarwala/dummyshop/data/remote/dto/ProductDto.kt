package com.nikunjagarwala.dummyshop.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DimensionsDto(
    val width: Double = 0.0,
    val height: Double = 0.0,
    val depth: Double = 0.0
)

@Serializable
data class ReviewDto(
    val rating: Int = 0,
    val comment: String = "",
    val date: String = "",
    val reviewerName: String = "",
    val reviewerEmail: String = ""
)

@Serializable
data class MetaDto(
    val createdAt: String = "",
    val updatedAt: String = "",
    val barcode: String = "",
    val qrCode: String = ""
)

@Serializable
data class ProductDto(
    val id: Int,
    val title: String,
    val description: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val discountPercentage: Double = 0.0,
    val rating: Double = 0.0,
    val stock: Int = 0,
    val tags: List<String> = emptyList(),
    val brand: String? = null,
    val sku: String = "",
    val weight: Double = 0.0,
    val dimensions: DimensionsDto = DimensionsDto(),
    val warrantyInformation: String = "",
    val shippingInformation: String = "",
    val availabilityStatus: String = "",
    val reviews: List<ReviewDto> = emptyList(),
    val returnPolicy: String = "",
    val minimumOrderQuantity: Int = 1,
    val meta: MetaDto = MetaDto(),
    val thumbnail: String = "",
    val images: List<String> = emptyList()
)

@Serializable
data class ProductListResponseDto(
    val products: List<ProductDto> = emptyList(),
    val total: Int = 0,
    val skip: Int = 0,
    val limit: Int = 0
)

@Serializable
data class CategoryDto(
    val slug: String,
    val name: String,
    val url: String = ""
)

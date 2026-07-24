package com.nikunjagarwala.dummyshop.testutil

import com.nikunjagarwala.dummyshop.data.remote.ProductApi
import com.nikunjagarwala.dummyshop.data.remote.dto.CategoryDto
import com.nikunjagarwala.dummyshop.data.remote.dto.ProductDto
import com.nikunjagarwala.dummyshop.data.remote.dto.ProductListResponseDto

/** Test double for [ProductApi]. Only override the members a given test actually exercises. */
open class FakeProductApi : ProductApi {
    override suspend fun getProducts(limit: Int, skip: Int, sortBy: String?, order: String?): ProductListResponseDto =
        error("getProducts not stubbed")

    override suspend fun searchProducts(query: String, limit: Int, skip: Int, sortBy: String?, order: String?): ProductListResponseDto =
        error("searchProducts not stubbed")

    override suspend fun getProductsByCategory(slug: String, limit: Int, skip: Int, sortBy: String?, order: String?): ProductListResponseDto =
        error("getProductsByCategory not stubbed")

    override suspend fun getCategories(): List<CategoryDto> = error("getCategories not stubbed")

    override suspend fun getProduct(id: Int): ProductDto = error("getProduct not stubbed")
}

fun sampleProductDto(id: Int, title: String = "Product $id") = ProductDto(
    id = id,
    title = title,
    description = "Description for $title",
    category = "smartphones",
    price = 100.0 + id,
    thumbnail = "https://example.com/$id.jpg"
)

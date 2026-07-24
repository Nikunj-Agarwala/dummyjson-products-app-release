package com.nikunjagarwala.dummyshop.data.remote

import com.nikunjagarwala.dummyshop.data.remote.dto.CategoryDto
import com.nikunjagarwala.dummyshop.data.remote.dto.ProductDto
import com.nikunjagarwala.dummyshop.data.remote.dto.ProductListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {

    @GET("products")
    suspend fun getProducts(
        @Query("limit") limit: Int,
        @Query("skip") skip: Int,
        @Query("sortBy") sortBy: String? = null,
        @Query("order") order: String? = null
    ): ProductListResponseDto

    @GET("products/search")
    suspend fun searchProducts(
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("skip") skip: Int,
        @Query("sortBy") sortBy: String? = null,
        @Query("order") order: String? = null
    ): ProductListResponseDto

    @GET("products/category/{slug}")
    suspend fun getProductsByCategory(
        @Path("slug") slug: String,
        @Query("limit") limit: Int,
        @Query("skip") skip: Int,
        @Query("sortBy") sortBy: String? = null,
        @Query("order") order: String? = null
    ): ProductListResponseDto

    @GET("products/categories")
    suspend fun getCategories(): List<CategoryDto>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Int): ProductDto

    companion object {
        const val BASE_URL = "https://dummyjson.com/"
    }
}

package com.nikunjagarwala.dummyshop.data.local

import com.nikunjagarwala.dummyshop.data.remote.dto.DimensionsDto
import com.nikunjagarwala.dummyshop.data.remote.dto.MetaDto
import com.nikunjagarwala.dummyshop.data.remote.dto.ReviewDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Nested product fields (tags, images, dimensions, reviews, meta) are stored as JSON text
 * columns rather than normalized tables since they're read-only, display-only data that is
 * always replaced wholesale whenever the product is refetched.
 */
object JsonCodec {
    val json = Json { ignoreUnknownKeys = true }

    fun encodeStrings(value: List<String>): String = json.encodeToString(value)
    fun decodeStrings(value: String): List<String> = json.decodeFromString(value)

    fun encodeDimensions(value: DimensionsDto): String = json.encodeToString(value)
    fun decodeDimensions(value: String): DimensionsDto = json.decodeFromString(value)

    fun encodeReviews(value: List<ReviewDto>): String = json.encodeToString(value)
    fun decodeReviews(value: String): List<ReviewDto> = json.decodeFromString(value)

    fun encodeMeta(value: MetaDto): String = json.encodeToString(value)
    fun decodeMeta(value: String): MetaDto = json.decodeFromString(value)
}

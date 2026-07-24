package com.nikunjagarwala.dummyshop.data.local

import com.nikunjagarwala.dummyshop.data.local.entity.ProductEntity
import com.nikunjagarwala.dummyshop.data.remote.dto.ProductDto
import com.nikunjagarwala.dummyshop.domain.Dimensions
import com.nikunjagarwala.dummyshop.domain.Product
import com.nikunjagarwala.dummyshop.domain.ProductMeta
import com.nikunjagarwala.dummyshop.domain.Review

fun ProductDto.toEntity(cachedAt: Long): ProductEntity = ProductEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    price = price,
    discountPercentage = discountPercentage,
    rating = rating,
    stock = stock,
    tagsJson = JsonCodec.encodeStrings(tags),
    brand = brand,
    sku = sku,
    weight = weight,
    dimensionsJson = JsonCodec.encodeDimensions(dimensions),
    warrantyInformation = warrantyInformation,
    shippingInformation = shippingInformation,
    availabilityStatus = availabilityStatus,
    reviewsJson = JsonCodec.encodeReviews(reviews),
    returnPolicy = returnPolicy,
    minimumOrderQuantity = minimumOrderQuantity,
    metaJson = JsonCodec.encodeMeta(meta),
    thumbnail = thumbnail,
    imagesJson = JsonCodec.encodeStrings(images),
    cachedAt = cachedAt
)

fun ProductEntity.toDomain(): Product {
    val dimensions = JsonCodec.decodeDimensions(dimensionsJson)
    val meta = JsonCodec.decodeMeta(metaJson)
    return Product(
        id = id,
        title = title,
        description = description,
        category = category,
        price = price,
        discountPercentage = discountPercentage,
        rating = rating,
        stock = stock,
        tags = JsonCodec.decodeStrings(tagsJson),
        brand = brand,
        sku = sku,
        weight = weight,
        dimensions = Dimensions(dimensions.width, dimensions.height, dimensions.depth),
        warrantyInformation = warrantyInformation,
        shippingInformation = shippingInformation,
        availabilityStatus = availabilityStatus,
        reviews = JsonCodec.decodeReviews(reviewsJson).map {
            Review(it.rating, it.comment, it.date, it.reviewerName, it.reviewerEmail)
        },
        returnPolicy = returnPolicy,
        minimumOrderQuantity = minimumOrderQuantity,
        meta = ProductMeta(meta.createdAt, meta.updatedAt, meta.barcode, meta.qrCode),
        thumbnail = thumbnail,
        images = JsonCodec.decodeStrings(imagesJson)
    )
}

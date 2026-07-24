package com.nikunjagarwala.dummyshop.data.local.entity

import androidx.room.Entity

/**
 * Bridge row recording where [productId] sits within the ordered results of [queryKey]
 * (a serialized search/filter/sort combination). Kept separate from [ProductEntity] so the
 * same cached product can appear in more than one list (e.g. "all" and a category filter)
 * without the rows fighting over ownership.
 */
@Entity(tableName = "list_entries", primaryKeys = ["queryKey", "position"])
data class ListEntryEntity(
    val queryKey: String,
    val position: Int,
    val productId: Int
)

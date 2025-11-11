package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Item(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String?,
    val estimatedValue: Double,
    val stock: Int,
    val bestBeforeDate: String?, // e.g., "2025-12-31"
    val useByDate: String?,
    val createdAt: String,
    val ownerId: String,
    val categoryId: String?,
    val owner: User?,
    // The ID of the circle this item is posted in
    val circleId: String?,
    // The full Circle object, if the API sends it nested
    val circle: Circle?
)
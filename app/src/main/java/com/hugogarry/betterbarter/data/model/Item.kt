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
    // You might include the full User object if your API sends it nested
    val owner: User?
)
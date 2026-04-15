package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateItemRequest(
    val name: String,
    val description: String,
    val estimatedValue: Double,
    val categoryId: String,
    val bestBeforeDate: String?,
    val useByDate: String?,
    val imageUrl: String,
    val stock: Int
)
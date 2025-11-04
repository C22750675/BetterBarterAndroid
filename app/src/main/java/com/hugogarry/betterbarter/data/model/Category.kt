package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Category(
    val id: String,
    val name: String,
    // Nullable to match the nullable parentCategory relationship on the backend
    val parentCategoryId: String?
)
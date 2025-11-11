package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val id: String,
    val username: String,
    val bio: String?,
    val profilePictureUrl: String?,
    val reputationScore: Double,
    val createdAt: String // e.g., "2025-10-10T10:00:00.000Z"
)
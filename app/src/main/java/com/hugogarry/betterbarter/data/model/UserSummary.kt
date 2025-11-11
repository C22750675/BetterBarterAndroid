package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.JsonClass

/**
 * Represents a partial User object, typically for nested lists
 * like the 'admins' array in a Circle.
 */
@JsonClass(generateAdapter = true)
data class UserSummary(
    val id: String,
    val username: String,
    val profilePictureUrl: String?
)
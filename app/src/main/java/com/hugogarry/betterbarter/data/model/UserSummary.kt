package com.hugogarry.betterbarter.data.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Represents a partial User object, typically for nested lists
 * like the 'admins' array in a Circle.
 */
@JsonClass(generateAdapter = true)
@Parcelize
data class UserSummary(
    val id: String,
    val username: String,
    val profilePictureUrl: String?
) : Parcelable
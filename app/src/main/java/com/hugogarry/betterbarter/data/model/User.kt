package com.hugogarry.betterbarter.data.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize


@JsonClass(generateAdapter = true)
@Parcelize
data class User(
    val id: String,
    val username: String,
    val email: String?,
    val phoneNumber: String?,
    val isEmailVerified: Boolean = false,
    val isPhoneVerified: Boolean = false,
    val bio: String?,
    val profilePictureUrl: String?,
    val reputationScore: Double,
    val lastReputationUpdate: String?, // ISO 8601 Timestamp
    val createdAt: String // e.g., "2025-10-10T10:00:00.000Z"
) : Parcelable
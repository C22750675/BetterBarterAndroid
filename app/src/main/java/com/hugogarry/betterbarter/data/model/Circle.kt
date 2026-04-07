package com.hugogarry.betterbarter.data.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class Circle(
    val id: String,
    val name: String,
    val origin: Point,
    val radius: Int,
    val reputationScore: Double,
    val minimumRepThreshold: Int,
    val createdAt: String,
    val memberCount: Int = 1,
    val color: String,
    val description: String?,
    val admins: List<UserSummary>?,
    val isMember: Boolean = false,
    val imageUrl: String?
) : Parcelable
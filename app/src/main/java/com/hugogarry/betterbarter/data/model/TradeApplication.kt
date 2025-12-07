package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.JsonClass
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class TradeApplication(
    val id: String,
    val tradeId: String,
    val status: String, // "pending", "accepted", etc.
    val message: String?,
    val offeredItemQuantity: Int,
    val offeredItemId: String,
    val applicantId: String,
    val createdAt: String,
    // Nested objects populated by the backend relations
    val applicant: User?,
    val offeredItem: Item?
) : Parcelable
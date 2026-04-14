package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.JsonClass

/**
 * Request model for updating an existing trade.
 * Note: circleId is omitted as trades cannot be moved between circles once created.
 */
@JsonClass(generateAdapter = true)
data class UpdateTradeRequest(
    val itemId: String,
    val quantity: Int,
    val description: String?
)
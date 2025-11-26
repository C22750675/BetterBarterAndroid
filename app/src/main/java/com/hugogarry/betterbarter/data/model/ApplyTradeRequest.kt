package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApplyTradeRequest(
    val offeredItemId: String,
    val offeredItemQuantity: Int,
    val message: String?
)
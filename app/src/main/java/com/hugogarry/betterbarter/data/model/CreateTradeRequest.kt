package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateTradeRequest(
    val itemId: String,
    val circleId: String,
    val quantity: Int,
    val description: String?
)
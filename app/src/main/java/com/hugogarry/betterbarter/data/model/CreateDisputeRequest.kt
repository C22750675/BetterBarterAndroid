package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateDisputeRequest(
    val tradeId: String,
    val description: String
)
package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Message(
    val id: String,
    val text: String,
    val imageUrl: String?,
    val senderId: String,
    val tradeId: String,
    val timestamp: String
)
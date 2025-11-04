package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Trade(
    val id: String,
    val status: TradeStatus,
    val creationDate: String,
    val completionDate: String?,
    val proposerId: String,
    val recipientId: String,
    // You can include nested objects if the API provides them
    val proposer: User?,
    val recipient: User?
)
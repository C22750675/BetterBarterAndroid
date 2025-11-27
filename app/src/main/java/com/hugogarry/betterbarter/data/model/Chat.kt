package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Chat(
    val id: String,
    val otherUser: UserSummary,
    val lastMessage: String?,
    val lastMessageTimestamp: String?,
    val tradeStatus: TradeStatus
)
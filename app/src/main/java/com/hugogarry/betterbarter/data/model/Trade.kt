package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Trade(
    val id: String,
    val status: TradeStatus,
    val creationDate: String,
    val completionDate: String?,
    val description: String?,
    val proposerId: String,
    val proposer: UserSummary,
    val recipientId: String?,
    val recipient: User?,
    val offeredItemQuantity: Int,
    val offeredItem: Item?,
    val circleId: String
)
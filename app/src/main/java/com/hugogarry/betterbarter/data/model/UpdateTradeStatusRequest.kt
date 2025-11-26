package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateTradeStatusRequest(
    val status: TradeStatus
)

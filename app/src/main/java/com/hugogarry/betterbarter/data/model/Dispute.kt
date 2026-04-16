package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Dispute(
    val id: String,
    val tradeId: String,
    val description: String,
    val status: String,
    val reporterId: String,
    val culpritId: String? = null,
    val severity: String? = null,
    val resolvedAt: String? = null,
    val createdAt: String,
    val trade: Trade? = null,
    val chatId: String? = null
)
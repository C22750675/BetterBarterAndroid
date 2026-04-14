package com.hugogarry.betterbarter.data.model

data class Dispute(
    val id: String,
    val tradeId: String,
    val chatId: String?,
    val initiator: UserSummary,
    val respondent: UserSummary,
    val reason: String,
    val status: String, // e.g., "OPEN", "RESOLVED"
    val createdAt: String
)
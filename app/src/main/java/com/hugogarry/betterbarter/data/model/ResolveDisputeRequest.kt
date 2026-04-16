package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ResolveDisputeRequest(
    val culpritId: String,
    val severity: DisputeSeverity,
    val resolutionNote: String
)
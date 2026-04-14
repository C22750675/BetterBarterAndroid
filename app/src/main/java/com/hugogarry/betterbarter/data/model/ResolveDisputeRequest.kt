package com.hugogarry.betterbarter.data.model

data class ResolveDisputeRequest(
    val culpritId: String,
    val severity: String,
    val resolutionNote: String
)
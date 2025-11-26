package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateRatingRequest(
    val score: Int,
    val comment: String? = null,
)
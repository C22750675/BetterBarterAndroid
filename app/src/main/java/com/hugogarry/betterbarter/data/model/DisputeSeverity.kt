package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.Json

enum class DisputeSeverity {
    @Json(name = "none") NONE,
    @Json(name = "low") LOW,
    @Json(name = "medium") MEDIUM,
    @Json(name = "high") HIGH
}
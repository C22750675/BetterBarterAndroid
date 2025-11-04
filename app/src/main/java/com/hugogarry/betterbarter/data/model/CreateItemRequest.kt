// In: data/model/CreateItemRequest.kt
package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateItemRequest(
    val name: String,
    val description: String,
    val estimatedValue: Double,
    // Add any other fields your backend requires
)
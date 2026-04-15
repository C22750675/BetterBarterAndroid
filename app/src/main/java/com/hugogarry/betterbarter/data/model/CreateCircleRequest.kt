package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.JsonClass

/**
 * Represents the GeoJSON Point structure for the request.
 */
@JsonClass(generateAdapter = true)
data class RequestPoint(
    val type: String = "Point",
    val coordinates: List<Double>
)

/**
 * The DTO (Data Transfer Object) to create a new circle.
 * This matches the backend's `CreateCircleDto`.
 */
@JsonClass(generateAdapter = true)
data class CreateCircleRequest(
    val name: String,
    val origin: RequestPoint,
    val radius: Int,
    val color: String,
    val description: String,
    val imageUrl: String
)
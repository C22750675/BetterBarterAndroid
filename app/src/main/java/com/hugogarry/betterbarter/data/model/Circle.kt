package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.JsonClass

// A simple class to hold GeoJSON Point coordinates
@JsonClass(generateAdapter = true)
data class Point(
    val type: String,
    val coordinates: List<Double> // [longitude, latitude]
)

@JsonClass(generateAdapter = true)
data class Circle(
    val id: String,
    val name: String,
    val origin: Point,
    val radius: Int,
    val reputationScore: Double,
    val minimumRepThreshold: Int,
    val createdAt: String,
    val memberCount: Int,
    val color: String
)
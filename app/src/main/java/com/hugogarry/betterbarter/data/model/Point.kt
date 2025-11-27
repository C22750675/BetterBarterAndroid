package com.hugogarry.betterbarter.data.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class Point(
    val type: String,
    val coordinates: List<Double> // [longitude, latitude]
) : Parcelable

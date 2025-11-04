package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginResponse(
    // The property names must match the keys in the JSON response from your backend
    val accessToken: String,
    val user: User
)
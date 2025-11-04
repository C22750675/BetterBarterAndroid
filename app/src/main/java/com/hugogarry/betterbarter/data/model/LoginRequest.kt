package com.hugogarry.betterbarter.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val username: String,
    val password: String
)
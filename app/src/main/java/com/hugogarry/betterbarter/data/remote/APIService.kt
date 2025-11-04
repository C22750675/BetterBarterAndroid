// In: data/remote/ApiService.kt
package com.hugogarry.betterbarter.data.remote

import com.hugogarry.betterbarter.data.model.Item
import com.hugogarry.betterbarter.data.model.Trade
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface APIService {

    /**
     * Fetches a list of items for a specific circle.
     * Example URL: https://your-api.com/api/circles/uuid-of-circle/items
     */
    @GET("circles/{circleId}/items")
    suspend fun getItemsForCircle(@Path("circleId") circleId: String): List<Item>

    /**
     * Fetches the details of a single item.
     * Example URL: https://your-api.com/api/items/uuid-of-item
     */
    @GET("items/{itemId}")
    suspend fun getItemDetails(@Path("itemId") itemId: String): Item

    /**
     * Creates a new trade. The Trade object will be converted to JSON.
     * The server's response (likely the newly created trade) is returned.
     */
    @POST("trades")
    suspend fun createTrade(@Body trade: Trade): Response<Trade>

    // ... Add other endpoints here for users, messages, ratings, etc.
    // For example:
    // @POST("auth/login")
    // suspend fun loginUser(@Body loginRequest: LoginRequest): LoginResponse
}
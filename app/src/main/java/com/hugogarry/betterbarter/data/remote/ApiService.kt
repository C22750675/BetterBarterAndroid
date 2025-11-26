package com.hugogarry.betterbarter.data.remote

import com.hugogarry.betterbarter.data.model.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("auth/register")
    suspend fun registerUser(@Body registerRequest: RegisterRequest): LoginResponse

    @POST("auth/login")
    suspend fun loginUser(@Body loginRequest: LoginRequest): LoginResponse

    @GET("auth/profile")
    suspend fun getProfile(): User

    @PATCH("auth/profile")
    suspend fun updateProfile(@Body updateProfileDto: UpdateProfileRequest): User

    // Items
    @GET("circles/{circleId}/items")
    suspend fun getItemsForCircle(@Path("circleId") circleId: String): List<Item>

    @GET("items/my-items")
    suspend fun getMyItems(): List<Item>

    @POST("items")
    suspend fun createItem(@Body createItemRequest: CreateItemRequest): Item

    @GET("items/categories")
    suspend fun getCategories(): List<Category>

    // Circles
    @GET("circles/{circleId}")
    suspend fun getCircleDetails(@Path("circleId") circleId: String): Circle

    @GET("circles/my-circles")
    suspend fun getMyCircles(): List<Circle>

    @POST("circles")
    suspend fun createCircle(@Body createCircleRequest: CreateCircleRequest): Circle

    @GET("circles/near")
    suspend fun findNearbyCircles(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("radius") radius: Int = 25000
    ): List<Circle>

    @POST("circles/{id}/join")
    suspend fun joinCircle(@Path("id") circleId: String): Void // Returns 201 Created

    // Trades
    @POST("trades")
    suspend fun createTrade(@Body createTradeRequest: CreateTradeRequest): Trade

    @GET("trades/circle/{circleId}")
    suspend fun getTradesForCircle(@Path("circleId") circleId: String): List<Trade>

    @GET("trades/my-trades")
    suspend fun getMyTrades(): List<Trade>

    @PATCH("trades/{id}/status")
    suspend fun updateTradeStatus(
        @Path("id") tradeId: String,
        @Body statusDto: UpdateTradeStatusRequest
    ): Trade

    @POST("trades/{id}/rate")
    suspend fun rateTrade(
        @Path("id") tradeId: String,
        @Body ratingDto: CreateRatingRequest
    ): Void

    // Upload
    @Multipart
    @POST("uploads")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): UploadResponse
}
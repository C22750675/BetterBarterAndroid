package com.hugogarry.betterbarter.data.remote

import com.hugogarry.betterbarter.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("auth/register")
    suspend fun registerUser(@Body registerRequest: RegisterRequest): Response<LoginResponse>

    @POST("auth/login")
    suspend fun loginUser(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @GET("auth/profile")
    suspend fun getProfile(): Response<User>

    @PATCH("auth/profile")
    suspend fun updateProfile(@Body updateProfileDto: UpdateProfileRequest): Response<User>

    // Items
    @GET("circles/{circleId}/items")
    suspend fun getItemsForCircle(@Path("circleId") circleId: String): Response<List<Item>>

    @GET("items/my-items")
    suspend fun getMyItems(): Response<List<Item>>

    @POST("items")
    suspend fun createItem(@Body createItemRequest: CreateItemRequest): Response<Item>

    @GET("items/categories")
    suspend fun getCategories(): Response<List<Category>>

    // Circles
    @GET("circles/{circleId}")
    suspend fun getCircleDetails(@Path("circleId") circleId: String): Response<Circle>

    @GET("circles/my-circles")
    suspend fun getMyCircles(): Response<List<Circle>>

    @POST("circles")
    suspend fun createCircle(@Body createCircleRequest: CreateCircleRequest): Response<Circle>

    @GET("circles/near")
    suspend fun findNearbyCircles(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("radius") radius: Int = 25000
    ): Response<List<Circle>>

    @POST("circles/{id}/join")
    suspend fun joinCircle(@Path("id") circleId: String): Response<Unit>

    // Trades
    @POST("trades")
    suspend fun createTrade(@Body createTradeRequest: CreateTradeRequest): Response<Trade>

    @PATCH("trades/{tradeId}")
    suspend fun updateTrade(
        @Path("tradeId") tradeId: String,
        @Body updateTradeRequest: UpdateTradeRequest
    ): Response<Trade>

    @DELETE("trades/{tradeId}")
    suspend fun deleteTrade(@Path("tradeId") tradeId: String): Response<Unit>

    @GET("trades/circle/{circleId}")
    suspend fun getTradesForCircle(@Path("circleId") circleId: String): Response<List<Trade>>

    @GET("trades/my-trades")
    suspend fun getMyTrades(): Response<List<Trade>>

    @PATCH("trades/{id}/status")
    suspend fun updateTradeStatus(
        @Path("id") tradeId: String,
        @Body statusDto: UpdateTradeStatusRequest
    ): Response<Trade>

    @POST("trades/{id}/rate")
    suspend fun rateTrade(
        @Path("id") tradeId: String,
        @Body ratingDto: CreateRatingRequest
    ): Response<Unit>

    @GET("trades/{id}")
    suspend fun getTrade(@Path("id") tradeId: String): Response<Trade>

    @POST("trades/{id}/apply")
    suspend fun applyForTrade(
        @Path("id") tradeId: String,
        @Body applyRequest: ApplyTradeRequest
    ): Response<TradeApplication>

    // Update existing trade application
    @PATCH("trades/applications/{id}")
    suspend fun updateApplication(
        @Path("id") applicationId: String,
        @Body applyRequest: ApplyTradeRequest
    ): Response<TradeApplication>

    @GET("trades/{id}/applications")
    suspend fun getTradeApplications(@Path("id") tradeId: String): Response<List<TradeApplication>>

    @POST("trades/applications/{id}/accept")
    suspend fun acceptApplication(@Path("id") applicationId: String): Response<Unit>

    @DELETE("trades/applications/{id}")
    suspend fun declineApplication(@Path("id") applicationId: String): Response<Unit>

    // Upload
    @Multipart
    @POST("uploads")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

    // Chat Endpoints
    @GET("chats")
    suspend fun getMyChats(): Response<List<Chat>>

    @GET("chats/{tradeId}/messages")
    suspend fun getMessages(@Path("tradeId") tradeId: String): Response<List<Message>>

    @POST("chats/{tradeId}/messages")
    suspend fun sendMessage(
        @Path("tradeId") tradeId: String,
        @Body message: Map<String, String>
    ): Response<Message>

    // Disputes

    @POST("disputes")
    suspend fun createDispute(@Body request: CreateDisputeRequest): Response<Void>

    @GET("disputes")
    suspend fun getDisputes(
        @Query("circleId") circleId: String? = null,
        @Query("status") status: String? = null
    ): Response<List<Dispute>>

    @GET("disputes/{id}")
    suspend fun getDisputeDetails(
        @Path("id") id: String
    ): Response<Dispute>

    @PATCH("disputes/{id}/resolve")
    suspend fun resolveDispute(
        @Path("id") id: String,
        @Body request: ResolveDisputeRequest
    ): Response<Dispute>
}
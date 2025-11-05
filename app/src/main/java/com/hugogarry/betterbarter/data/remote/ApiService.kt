package com.hugogarry.betterbarter.data.remote

import com.hugogarry.betterbarter.data.model.Category
import com.hugogarry.betterbarter.data.model.CreateItemRequest
import com.hugogarry.betterbarter.data.model.Item
import com.hugogarry.betterbarter.data.model.Trade
import com.hugogarry.betterbarter.data.model.LoginRequest
import com.hugogarry.betterbarter.data.model.LoginResponse
import com.hugogarry.betterbarter.data.model.RegisterRequest
import com.hugogarry.betterbarter.data.model.UploadResponse
import com.hugogarry.betterbarter.data.model.User
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import com.hugogarry.betterbarter.data.model.UpdateProfileRequest


interface ApiService {

    @POST("auth/register")
    suspend fun registerUser(@Body registerRequest: RegisterRequest): LoginResponse

    @POST("auth/login")
    suspend fun loginUser(@Body loginRequest: LoginRequest): LoginResponse

    // Fetches a list of items for a specific circle.
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

    @GET("auth/profile") // We'll use this to get the user's profile data
    suspend fun getProfile(): User // Assuming this returns a User object

    @GET("items/my-items")
    suspend fun getMyItems(): List<Item>

    @POST("items")
    suspend fun createItem(@Body createItemRequest: CreateItemRequest): Item

    @GET("items/categories")
    suspend fun getCategories(): List<Category>

    @Multipart
    @POST("uploads")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): UploadResponse

    // Endpoint to update the user's profile
    @PATCH("auth/profile")
    suspend fun updateProfile(@Body updateProfileDto: UpdateProfileRequest): User
}

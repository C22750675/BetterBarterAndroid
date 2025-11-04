package com.hugogarry.betterbarter.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ApiClient {

    // IMPORTANT: REPLACE WITH YOUR BACKEND'S URL
    private const val BASE_URL = "http://10.0.2.2:3000/api/"

    // Initialize Moshi for JSON parsing, adding the Kotlin adapter
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Initialize a lazy Retrofit instance
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    // Publicly expose the ApiService interface implementation
    val apiService: APIService by lazy {
        retrofit.create(APIService::class.java)
    }
}
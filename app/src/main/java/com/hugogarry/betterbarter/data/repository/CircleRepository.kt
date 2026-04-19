package com.hugogarry.betterbarter.data.repository

import com.hugogarry.betterbarter.data.model.Circle
import com.hugogarry.betterbarter.data.model.CreateCircleRequest
import com.hugogarry.betterbarter.data.model.JoinCircleRequest
import com.hugogarry.betterbarter.data.remote.ApiService
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CircleRepository(private val apiService: ApiService) {

    suspend fun getCircleDetails(circleId: String): Resource<Circle> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCircleDetails(circleId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message() ?: "Failed to fetch circle details")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun getMyCircles(): Resource<List<Circle>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMyCircles()
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error(response.message() ?: "Failed to fetch your circles")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun createCircle(request: CreateCircleRequest): Resource<Circle> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createCircle(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                // Check for the 409 Conflict status code and return our custom message
                if (response.code() == 409) {
                    Resource.Error("A circle with this name already exists. Please choose another.")
                } else {
                    Resource.Error(response.message() ?: "Failed to create circle")
                }
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun findNearbyCircles(lat: Double, lon: Double, radius: Int = 25000): Resource<List<Circle>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.findNearbyCircles(lat, lon, radius)
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error(response.message() ?: "Failed to fetch nearby circles")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun joinCircle(circleId: String, lat: Double, lon: Double): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = JoinCircleRequest(lat, lon)
            val response = apiService.joinCircle(circleId, request)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                // Extract the detailed message from the NestJS JSON error body
                val errorMessage = try {
                    val errorString = response.errorBody()?.string()
                    if (!errorString.isNullOrEmpty()) {
                        org.json.JSONObject(errorString).getString("message")
                    } else {
                        response.message()
                    }
                } catch (_: Exception) {
                    response.message()
                }

                Resource.Error(errorMessage ?: "Failed to join circle")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }
}
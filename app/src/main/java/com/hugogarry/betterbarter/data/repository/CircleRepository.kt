package com.hugogarry.betterbarter.data.repository

import com.hugogarry.betterbarter.data.model.Circle
import com.hugogarry.betterbarter.data.model.CreateCircleRequest
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
                Resource.Error(response.message() ?: "Failed to create circle")
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

    suspend fun joinCircle(circleId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.joinCircle(circleId)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.message() ?: "Failed to join circle")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }
}
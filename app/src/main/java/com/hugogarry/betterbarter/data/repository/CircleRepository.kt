package com.hugogarry.betterbarter.data.repository

import com.hugogarry.betterbarter.data.model.Circle
import com.hugogarry.betterbarter.data.model.CreateCircleRequest
import com.hugogarry.betterbarter.data.remote.ApiClient
import com.hugogarry.betterbarter.data.remote.ApiService
import com.hugogarry.betterbarter.util.Resource
import retrofit2.HttpException
import java.io.IOException

class CircleRepository(private val apiService: ApiService = ApiClient.apiService) {

    suspend fun joinCircle(circleId: String): Resource<Boolean> {
        return try {
            apiService.joinCircle(circleId)
            Resource.Success(true)
        } catch (e: HttpException) {
            val msg = when(e.code()) {
                403 -> "You do not meet the requirements to join this circle."
                409 -> "You are already a member."
                else -> "Failed to join circle."
            }
            Resource.Error(msg)
        } catch (e: Exception) {
            Resource.Error("Network error.")
        }
    }

    suspend fun getCircleDetails(circleId: String): Resource<Circle> {
        return try {
            val circle = apiService.getCircleDetails(circleId)
            Resource.Success(circle)
        } catch (e: Exception) { Resource.Error(e.message ?: "Error") }
    }

    suspend fun getMyCircles(): Resource<List<Circle>> {
        return try {
            val circles = apiService.getMyCircles()
            Resource.Success(circles)
        } catch (e: Exception) { Resource.Error(e.message ?: "Error") }
    }

    suspend fun createCircle(request: CreateCircleRequest): Resource<Circle> {
        return try {
            val newCircle = apiService.createCircle(request)
            Resource.Success(newCircle)
        } catch (e: Exception) { Resource.Error(e.message ?: "Error") }
    }

    suspend fun findNearbyCircles(latitude: Double, longitude: Double): Resource<List<Circle>> {
        return try {
            val circles = apiService.findNearbyCircles(latitude, longitude)
            Resource.Success(circles)
        } catch (e: Exception) { Resource.Error(e.message ?: "Error") }
    }
}
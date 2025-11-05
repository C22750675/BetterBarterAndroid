package com.hugogarry.betterbarter.data.repository

import com.hugogarry.betterbarter.data.model.Circle
import com.hugogarry.betterbarter.data.remote.ApiClient
import com.hugogarry.betterbarter.data.remote.ApiService
import com.hugogarry.betterbarter.util.Resource
import retrofit2.HttpException
import java.io.IOException
import com.hugogarry.betterbarter.data.model.CreateCircleRequest

class CircleRepository(private val apiService: ApiService = ApiClient.apiService) {

    suspend fun getMyCircles(): Resource<List<Circle>> {
        return try {
            val circles = apiService.getMyCircles()
            Resource.Success(circles)
        } catch (e: IOException) {
            Resource.Error("Network error: Could not fetch circles. Please check your connection.")
        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                401 -> "Unauthorized. Please log in again."
                404 -> "No circles found."
                500 -> "Server error. Please try again later."
                else -> "An unexpected error occurred: ${e.message()}"
            }
            Resource.Error(errorMsg)
        }
    }

    suspend fun createCircle(request: CreateCircleRequest): Resource<Circle> {
        return try {
            val newCircle = apiService.createCircle(request)
            Resource.Success(newCircle)
        } catch (e: IOException) {
            Resource.Error("Network error: Could not create circle.")
        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                400 -> "Invalid circle data. Please check your inputs."
                401 -> "Unauthorized. Please log in again."
                else -> "An unexpected error occurred: ${e.message()}"
            }
            Resource.Error(errorMsg)
        }
    }

    suspend fun findNearbyCircles(latitude: Double, longitude: Double): Resource<List<Circle>> {
        return try {
            // We can hardcode the radius for now or pass it in
            val circles = apiService.findNearbyCircles(latitude, longitude, radius = 50000) // 50km
            Resource.Success(circles)
        } catch (e: IOException) {
            Resource.Error("Network error: Could not find circles.")
        } catch (e: HttpException) {
            Resource.Error("An unexpected error occurred: ${e.message()}")
        }
    }
}
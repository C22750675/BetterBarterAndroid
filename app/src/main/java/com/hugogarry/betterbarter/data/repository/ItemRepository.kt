package com.hugogarry.betterbarter.data.repository

import com.hugogarry.betterbarter.data.model.Category
import com.hugogarry.betterbarter.data.model.CreateItemRequest // Import CreateItemRequest
import com.hugogarry.betterbarter.data.model.Item
import com.hugogarry.betterbarter.data.remote.ApiClient
import com.hugogarry.betterbarter.data.remote.ApiService
import com.hugogarry.betterbarter.util.Resource
import java.io.IOException
import retrofit2.HttpException

/**
 * Repository for handling item-related data operations.
 * It uses the ApiService to fetch data from the network.
 *
 * @param apiService The Retrofit service interface. Defaults to the singleton ApiClient.
 */
class ItemRepository(private val apiService: ApiService = ApiClient.apiService) {

    /**
     * Fetches a list of items for a given circle from the API.
     *
     * @param circleId The UUID of the circle.
     * @return A Resource wrapper containing either the list of items on success
     * or an error message on failure.
     */
    suspend fun getItemsForCircle(circleId: String): Resource<List<Item>> {
        return try {
            // Make the network call
            val items = apiService.getItemsForCircle(circleId)
            // If the call is successful, wrap the data in Resource.Success
            Resource.Success(items)
        } catch (e: IOException) {
            // This catches network errors (e.g., no internet connection)
            Resource.Error("Network error: Please check your internet connection.")
        } catch (e: HttpException) {
            // This catches non-2xx HTTP responses (e.g., 404 Not Found, 500 Server Error)
            val errorMessage = when (e.code()) {
                404 -> "Circle not found."
                500 -> "Server error. Please try again later."
                else -> "An unexpected error occurred: ${e.message()}"
            }
            Resource.Error(errorMessage)
        }
    }

    suspend fun getMyItems(): Resource<List<Item>> {
        return try {
            Resource.Success(apiService.getMyItems())
        } catch (e: Exception) {
            Resource.Error("Failed to fetch your items: ${e.message}")
        }
    }

    /**
     * Creates a new item on the backend via the API.
     */
    suspend fun createItem(request: CreateItemRequest): Resource<Item> {
        return try {
            val item = apiService.createItem(request)
            Resource.Success(item)
        } catch (e: IOException) {
            Resource.Error("Network error: Please check your internet connection.")
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                400 -> "Invalid item data provided. Please check your inputs."
                401 -> "Authentication failed. Please log in again."
                else -> "Failed to create item: ${e.message()}"
            }
            Resource.Error(errorMessage)
        }
    }

    suspend fun getCategories(): Resource<List<Category>> {
        return try {
            val categories = apiService.getCategories()
            Resource.Success(categories)
        } catch (e: IOException) {
            Resource.Error("Network error: Could not fetch categories. Please check your connection.")
        } catch (e: HttpException) {
            Resource.Error("Failed to fetch categories: ${e.message()}")
        }
    }
}
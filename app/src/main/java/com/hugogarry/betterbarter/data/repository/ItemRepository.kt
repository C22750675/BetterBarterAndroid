package com.hugogarry.betterbarter.data.repository

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
     *         or an error message on failure.
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

    // You would add other item-related functions here, for example:
    /*
    suspend fun getItemDetails(itemId: String): Resource<Item> {
        // ... implementation with try-catch block ...
    }
    */
}
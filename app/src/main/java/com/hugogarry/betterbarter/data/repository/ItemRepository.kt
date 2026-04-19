package com.hugogarry.betterbarter.data.repository

import com.hugogarry.betterbarter.data.model.Category
import com.hugogarry.betterbarter.data.model.CreateItemRequest
import com.hugogarry.betterbarter.data.model.Item
import com.hugogarry.betterbarter.data.remote.ApiService
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class ItemRepository(private val apiService: ApiService) {

    suspend fun getItemsForCircle(circleId: String): Resource<List<Item>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getItemsForCircle(circleId)
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string()) ?: response.message() ?: "Failed to fetch items"
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun getMyItems(): Resource<List<Item>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMyItems()
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string()) ?: response.message() ?: "Failed to fetch your items"
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun createItem(request: CreateItemRequest): Resource<Item> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createItem(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                // Parse the specific error body returned by the API
                val errorMsg = parseErrorMessage(response.errorBody()?.string()) ?: response.message() ?: "Failed to create item"
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun getCategories(): Resource<List<Category>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCategories()
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string()) ?: response.message() ?: "Failed to fetch categories"
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    /**
     * Parses the JSON error body from the backend to extract a readable message.
     * Handles both single string messages and arrays of validation messages.
     */
    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val jsonObject = JSONObject(errorBody)
            if (jsonObject.has("message")) {
                val messageObj = jsonObject.get("message")
                if (messageObj is JSONArray) {
                    // If it's an array of errors (like ClassValidator returns), join them into a list
                    val messages = mutableListOf<String>()
                    for (i in 0 until messageObj.length()) {
                        messages.add(messageObj.getString(i))
                    }
                    messages.joinToString("\n")
                } else {
                    // If it's just a regular string message
                    jsonObject.getString("message")
                }
            } else {
                null
            }
        } catch (_: Exception) {
            null // Fallback to default response.message() if parsing fails
        }
    }
}
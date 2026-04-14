package com.hugogarry.betterbarter.data.repository

import com.hugogarry.betterbarter.data.model.Category
import com.hugogarry.betterbarter.data.model.CreateItemRequest
import com.hugogarry.betterbarter.data.model.Item
import com.hugogarry.betterbarter.data.remote.ApiService
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItemRepository(private val apiService: ApiService) {

    suspend fun getItemsForCircle(circleId: String): Resource<List<Item>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getItemsForCircle(circleId)
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error(response.message() ?: "Failed to fetch items")
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
                Resource.Error(response.message() ?: "Failed to fetch your items")
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
                Resource.Error(response.message() ?: "Failed to create item")
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
                Resource.Error(response.message() ?: "Failed to fetch categories")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }
}
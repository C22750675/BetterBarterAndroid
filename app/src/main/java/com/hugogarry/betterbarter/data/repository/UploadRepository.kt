package com.hugogarry.betterbarter.data.repository

import com.hugogarry.betterbarter.data.model.UploadResponse
import com.hugogarry.betterbarter.data.remote.ApiService
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody

class UploadRepository(private val apiService: ApiService) {

    suspend fun uploadImage(file: MultipartBody.Part): Resource<UploadResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.uploadImage(file)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message() ?: "Failed to upload image")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }
}
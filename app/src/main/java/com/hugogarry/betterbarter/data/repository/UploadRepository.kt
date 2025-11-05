package com.hugogarry.betterbarter.data.repository

import com.hugogarry.betterbarter.data.model.UploadResponse
import com.hugogarry.betterbarter.data.remote.ApiClient
import com.hugogarry.betterbarter.data.remote.ApiService
import com.hugogarry.betterbarter.util.Resource
import okhttp3.MultipartBody
import java.io.IOException

class UploadRepository(private val apiService: ApiService = ApiClient.apiService) {

    suspend fun uploadImage(filePart: MultipartBody.Part): Resource<UploadResponse> {
        return try {
            val response = apiService.uploadImage(filePart)
            Resource.Success(response)
        } catch (e: IOException) {
            Resource.Error("Network error: Could not upload image.")
        } catch (e: Exception) {
            Resource.Error("Upload failed: ${e.message}")
        }
    }
}
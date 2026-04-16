package com.hugogarry.betterbarter.data.repository

import com.hugogarry.betterbarter.data.model.CreateDisputeRequest
import com.hugogarry.betterbarter.data.model.Dispute
import com.hugogarry.betterbarter.data.model.ResolveDisputeRequest
import com.hugogarry.betterbarter.data.remote.ApiService
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DisputeRepository(private val apiService: ApiService) {

    suspend fun getDisputes(circleId: String? = null): Resource<List<Dispute>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDisputes(circleId = circleId)
                if (response.isSuccessful) {
                    Resource.Success(response.body() ?: emptyList())
                } else {
                    Resource.Error(response.message() ?: "Failed to fetch disputes")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun getDisputeDetails(id: String): Resource<Dispute> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDisputeDetails(id)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error(response.message() ?: "Failed to fetch dispute details")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun resolveDispute(id: String, request: ResolveDisputeRequest): Resource<Dispute> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.resolveDispute(id, request)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error(response.message() ?: "Failed to resolve dispute")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    // Now returns Resource<Unit> because we ignore the response body
    suspend fun createDispute(request: CreateDisputeRequest): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createDispute(request)
                if (response.isSuccessful) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(response.message() ?: "Failed to create dispute")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }
}
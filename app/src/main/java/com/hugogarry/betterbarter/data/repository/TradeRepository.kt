package com.hugogarry.betterbarter.data.repository

import com.hugogarry.betterbarter.data.model.*
import com.hugogarry.betterbarter.data.remote.ApiService
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TradeRepository(private val apiService: ApiService) {

    suspend fun createTrade(request: CreateTradeRequest): Resource<Trade> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createTrade(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message() ?: "Failed to create trade")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun updateTrade(tradeId: String, request: UpdateTradeRequest): Resource<Trade> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateTrade(tradeId, request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message() ?: "Failed to update trade")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun deleteTrade(tradeId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteTrade(tradeId)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.message() ?: "Failed to delete trade")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun getTradesForCircle(circleId: String): Resource<List<Trade>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTradesForCircle(circleId)
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error(response.message() ?: "Failed to fetch trades")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun getMyTrades(): Resource<List<Trade>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMyTrades()
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error(response.message() ?: "Failed to fetch your trades")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun updateTradeStatus(tradeId: String, request: UpdateTradeStatusRequest): Resource<Trade> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateTradeStatus(tradeId, request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message() ?: "Failed to update trade status")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun rateTrade(tradeId: String, request: CreateRatingRequest): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.rateTrade(tradeId, request)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.message() ?: "Failed to submit rating")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun getTrade(tradeId: String): Resource<Trade> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTrade(tradeId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message() ?: "Failed to fetch trade details")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun applyForTrade(tradeId: String, request: ApplyTradeRequest): Resource<TradeApplication> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.applyForTrade(tradeId, request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message() ?: "Failed to apply for trade")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun getTradeApplications(tradeId: String): Resource<List<TradeApplication>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTradeApplications(tradeId)
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error(response.message() ?: "Failed to fetch applications")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun acceptApplication(applicationId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.acceptApplication(applicationId)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.message() ?: "Failed to accept application")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun declineApplication(applicationId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.declineApplication(applicationId)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.message() ?: "Failed to decline application")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }
}
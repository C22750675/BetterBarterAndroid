package com.hugogarry.betterbarter.data.repository

import com.hugogarry.betterbarter.data.model.*
import com.hugogarry.betterbarter.data.remote.ApiService
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class TradeRepository(private val apiService: ApiService) {

    suspend fun createTrade(request: CreateTradeRequest): Resource<Trade> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createTrade(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(extractErrorMessage(response, "Failed to create trade"))
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
                Resource.Error(extractErrorMessage(response, "Failed to update trade"))
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
                Resource.Error(extractErrorMessage(response, "Failed to delete trade"))
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
                Resource.Error(extractErrorMessage(response, "Failed to fetch trades"))
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
                Resource.Error(extractErrorMessage(response, "Failed to fetch your trades"))
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
                Resource.Error(extractErrorMessage(response, "Failed to update trade status"))
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
                Resource.Error(extractErrorMessage(response, "Failed to submit rating"))
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
                Resource.Error(extractErrorMessage(response, "Failed to fetch trade details"))
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
                Resource.Error(extractErrorMessage(response, "Failed to apply for trade"))
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun updateApplication(applicationId: String, request: ApplyTradeRequest): Resource<TradeApplication> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateApplication(applicationId, request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(extractErrorMessage(response, "Failed to update application"))
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
                Resource.Error(extractErrorMessage(response, "Failed to fetch applications"))
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
                Resource.Error(extractErrorMessage(response, "Failed to accept application"))
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
                Resource.Error(extractErrorMessage(response, "Failed to decline application"))
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    /**
     * Helper function to extract detailed error messages from the backend's JSON response body.
     * Retrofit's response.message() only returns the standard HTTP status text (e.g., "Bad Request").
     */
    private fun extractErrorMessage(response: retrofit2.Response<*>, fallbackMessage: String): String {
        return try {
            val errorBodyString = response.errorBody()?.string()
            if (!errorBodyString.isNullOrEmpty()) {
                val jsonObject = JSONObject(errorBodyString)
                if (jsonObject.has("message")) {
                    val messageOpt = jsonObject.get("message")
                    // Sometimes backend validation errors return an array of strings
                    if (messageOpt is JSONArray && messageOpt.length() > 0) {
                        messageOpt.getString(0)
                    } else {
                        messageOpt.toString()
                    }
                } else {
                    response.message().takeIf { it.isNotBlank() } ?: fallbackMessage
                }
            } else {
                response.message().takeIf { it.isNotBlank() } ?: fallbackMessage
            }
        } catch (_: Exception) {
            response.message().takeIf { it.isNotBlank() } ?: fallbackMessage
        }
    }
}
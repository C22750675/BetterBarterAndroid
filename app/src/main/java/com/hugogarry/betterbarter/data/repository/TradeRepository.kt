package com.hugogarry.betterbarter.data.repository

import com.hugogarry.betterbarter.data.model.CreateTradeRequest
import com.hugogarry.betterbarter.data.model.UpdateTradeRequest
import com.hugogarry.betterbarter.data.model.Trade
import com.hugogarry.betterbarter.data.model.TradeStatus
import com.hugogarry.betterbarter.data.remote.ApiClient
import com.hugogarry.betterbarter.data.remote.ApiService
import com.hugogarry.betterbarter.util.Resource
import com.hugogarry.betterbarter.data.model.UpdateTradeStatusRequest
import com.hugogarry.betterbarter.data.model.CreateRatingRequest
import com.hugogarry.betterbarter.data.model.ApplyTradeRequest
import com.hugogarry.betterbarter.data.model.TradeApplication

class TradeRepository(private val apiService: ApiService = ApiClient.apiService) {

    suspend fun createTrade(circleId: String, itemId: String, quantity: Int, description: String): Resource<Trade> {
        return try {
            val request = CreateTradeRequest(itemId, circleId, quantity, description)
            val trade = apiService.createTrade(request)
            Resource.Success(trade)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create trade")
        }
    }

    /**
     * Updates an existing trade proposal.
     */
    suspend fun updateTrade(tradeId: String, itemId: String, quantity: Int, description: String): Resource<Trade> {
        return try {
            val request = UpdateTradeRequest(itemId, quantity, description)
            val trade = apiService.updateTrade(tradeId, request)
            Resource.Success(trade)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update trade proposal")
        }
    }

    /**
     * Deletes a trade proposal and returns stock to the user's inventory.
     */
    suspend fun deleteTrade(tradeId: String): Resource<Unit> {
        return try {
            apiService.deleteTrade(tradeId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete trade proposal")
        }
    }

    suspend fun getTradesForCircle(circleId: String): Resource<List<Trade>> {
        return try {
            val trades = apiService.getTradesForCircle(circleId)
            Resource.Success(trades)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load trades")
        }
    }

    suspend fun getMyTrades(): Resource<List<Trade>> {
        return try {
            val trades = apiService.getMyTrades()
            Resource.Success(trades)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load your trades")
        }
    }

    suspend fun updateStatus(tradeId: String, status: TradeStatus): Resource<Trade> {
        return try {
            val trade = apiService.updateTradeStatus(tradeId, UpdateTradeStatusRequest(status))
            Resource.Success(trade)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update status")
        }
    }

    suspend fun rateTrade(tradeId: String, score: Int, comment: String): Resource<Boolean> {
        return try {
            apiService.rateTrade(tradeId, CreateRatingRequest(score, comment))
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to submit review")
        }
    }

    suspend fun getTrade(tradeId: String): Resource<Trade> {
        return try {
            val trade = apiService.getTrade(tradeId)
            Resource.Success(trade)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load trade details")
        }
    }

    suspend fun applyForTrade(tradeId: String, itemId: String, quantity: Int, message: String): Resource<TradeApplication> {
        return try {
            val request = ApplyTradeRequest(itemId, quantity, message)
            val response = apiService.applyForTrade(tradeId, request)
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to apply for trade")
        }
    }

    suspend fun getTradeApplications(tradeId: String): Resource<List<TradeApplication>> {
        return try {
            val applications = apiService.getTradeApplications(tradeId)
            Resource.Success(applications)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load applications")
        }
    }

    suspend fun acceptApplication(applicationId: String): Resource<Boolean> {
        return try {
            apiService.acceptApplication(applicationId)
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to accept application")
        }
    }

    suspend fun declineApplication(applicationId: String): Resource<Boolean> {
        return try {
            apiService.declineApplication(applicationId)
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to decline application")
        }
    }
}
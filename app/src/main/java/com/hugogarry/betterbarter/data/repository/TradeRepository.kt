package com.hugogarry.betterbarter.data.repository

import com.hugogarry.betterbarter.data.model.CreateRatingRequest
import com.hugogarry.betterbarter.data.model.CreateTradeRequest
import com.hugogarry.betterbarter.data.model.Trade
import com.hugogarry.betterbarter.data.model.TradeStatus
import com.hugogarry.betterbarter.data.model.UpdateTradeStatusRequest
import com.hugogarry.betterbarter.data.remote.ApiClient
import com.hugogarry.betterbarter.data.remote.ApiService
import com.hugogarry.betterbarter.util.Resource


class TradeRepository(private val apiService: ApiService = ApiClient.apiService) {

    suspend fun createTrade(createTradeRequest: CreateTradeRequest): Resource<Trade> {
        return try {
            val trade = apiService.createTrade(createTradeRequest)
            Resource.Success(trade)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create trade")
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
}
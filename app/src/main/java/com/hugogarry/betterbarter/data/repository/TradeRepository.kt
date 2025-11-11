package com.hugogarry.betterbarter.data.repository

import com.hugogarry.betterbarter.data.model.CreateTradeRequest
import com.hugogarry.betterbarter.data.model.Trade
import com.hugogarry.betterbarter.data.remote.ApiClient
import com.hugogarry.betterbarter.data.remote.ApiService
import com.hugogarry.betterbarter.util.Resource
import retrofit2.HttpException
import java.io.IOException

class TradeRepository(private val apiService: ApiService = ApiClient.apiService) {

    /**
     * Creates a new trade proposal
     */
    suspend fun createTrade(createTradeRequest: CreateTradeRequest): Resource<Trade> {
        return try {
            val trade = apiService.createTrade(createTradeRequest)
            Resource.Success(trade)
        } catch (e: IOException) {
            Resource.Error("Network error: Could not create trade.")
        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                400 -> "Invalid data. Do you have enough stock?"
                403 -> "You do not own this item."
                else -> "An unexpected error occurred: ${e.message()}"
            }
            Resource.Error(errorMsg)
        }
    }

    /**
     * Fetches all active trades for a given circle
     */
    suspend fun getTradesForCircle(circleId: String): Resource<List<Trade>> {
        return try {
            val trades = apiService.getTradesForCircle(circleId)
            Resource.Success(trades)
        } catch (e: IOException) {
            Resource.Error("Network error: Could not load trades.")
        } catch (e: HttpException) {
            Resource.Error("An unexpected error occurred: ${e.message()}")
        }
    }
}
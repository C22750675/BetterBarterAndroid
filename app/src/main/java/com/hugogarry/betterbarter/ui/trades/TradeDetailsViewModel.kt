package com.hugogarry.betterbarter.ui.trades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.Circle
import com.hugogarry.betterbarter.data.model.CreateRatingRequest
import com.hugogarry.betterbarter.data.model.Trade
import com.hugogarry.betterbarter.data.remote.ApiClient
import com.hugogarry.betterbarter.data.repository.AuthRepository
import com.hugogarry.betterbarter.data.repository.CircleRepository
import com.hugogarry.betterbarter.data.repository.TradeRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TradeDetailsUiState(
    val trade: Trade? = null,
    val circle: Circle? = null, // ADDED: To hold the circle details
    val isMember: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class TradeDetailsViewModel(
    private val tradeRepository: TradeRepository = TradeRepository(ApiClient.apiService),
    private val circleRepository: CircleRepository = CircleRepository(ApiClient.apiService),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TradeDetailsUiState())
    val uiState: StateFlow<TradeDetailsUiState> = _uiState

    private val _ratingState = MutableStateFlow<Resource<Unit>>(Resource.Idle())
    val ratingState: StateFlow<Resource<Unit>> = _ratingState

    fun fetchTradeDetails(tradeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val tradeResult = tradeRepository.getTrade(tradeId)
            if (tradeResult is Resource.Success) {
                val trade = tradeResult.data!!

                // Parallel calls to verify membership status and get circle details robustly
                val circleResultDeferred = async { circleRepository.getCircleDetails(trade.circleId) }
                val myCirclesResultDeferred = async { circleRepository.getMyCircles() }
                val profileResultDeferred = async { authRepository.getProfile() }

                val circleResult = circleResultDeferred.await()
                val myCirclesResult = myCirclesResultDeferred.await()
                val profileResult = profileResultDeferred.await()

                // Capture circle details
                val circleData = (circleResult as? Resource.Success)?.data
                val myCircles = (myCirclesResult as? Resource.Success)?.data ?: emptyList()
                val user = (profileResult as? Resource.Success)?.data

                // Calculate membership using the same logic as the Circle screen
                val isUserAdmin = circleData?.admins?.any { it.id == user?.id } ?: false
                val isExplicitMember = circleData?.isMember ?: false
                val isInMyCirclesList = myCircles.any { it.id == trade.circleId }

                val isMember = isUserAdmin || isExplicitMember || isInMyCirclesList

                _uiState.value = TradeDetailsUiState(
                    trade = trade,
                    circle = circleData, // ASSIGNED: Passing the circle data to the UI
                    isMember = isMember,
                    isLoading = false
                )
            } else if (tradeResult is Resource.Error) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = tradeResult.message
                )
            }
        }
    }

    fun rateTrade(tradeId: String, score: Int, comment: String) {
        viewModelScope.launch {
            if (score !in 1..5) {
                _ratingState.value = Resource.Error("Score must be between 1 and 5")
                return@launch
            }

            _ratingState.value = Resource.Loading()
            val request = CreateRatingRequest(score, comment)
            val result = tradeRepository.rateTrade(tradeId, request)
            _ratingState.value = result

            if (result is Resource.Success) {
                fetchTradeDetails(tradeId)
            }
        }
    }
}
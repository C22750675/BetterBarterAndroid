package com.hugogarry.betterbarter.ui.trades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.Trade
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
    val isMember: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class TradeDetailsViewModel(
    private val tradeRepository: TradeRepository = TradeRepository(),
    private val circleRepository: CircleRepository = CircleRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TradeDetailsUiState())
    val uiState: StateFlow<TradeDetailsUiState> = _uiState

    // Dedicated state for rating submissions
    private val _ratingState = MutableStateFlow<Resource<Boolean>>(Resource.Idle())
    val ratingState: StateFlow<Resource<Boolean>> = _ratingState

    fun fetchTradeDetails(tradeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val tradeResult = tradeRepository.getTrade(tradeId)
            if (tradeResult is Resource.Success) {
                val trade = tradeResult.data!!

                // Parallel calls to verify membership status robustly
                val circleResultDeferred = async { circleRepository.getCircleDetails(trade.circleId) }
                val myCirclesResultDeferred = async { circleRepository.getMyCircles() }
                val profileResultDeferred = async { authRepository.getProfile() }

                val circleResult = circleResultDeferred.await()
                val myCirclesResult = myCirclesResultDeferred.await()
                val profileResult = profileResultDeferred.await()

                // Calculate membership using the same logic as the Circle screen
                val circleData = (circleResult as? Resource.Success)?.data
                val myCircles = (myCirclesResult as? Resource.Success)?.data ?: emptyList()
                val user = (profileResult as? Resource.Success)?.data

                val isUserAdmin = circleData?.admins?.any { it.id == user?.id } ?: false
                val isExplicitMember = circleData?.isMember ?: false
                val isInMyCirclesList = myCircles.any { it.id == trade.circleId }

                // The robust check
                val isMember = isUserAdmin || isExplicitMember || isInMyCirclesList

                _uiState.value = TradeDetailsUiState(
                    trade = trade,
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

            // Call the repository to submit the rating
            val result = tradeRepository.rateTrade(tradeId, score, comment)
            _ratingState.value = result

            // Refresh the trade if the rating was successful,
            // fetching updated rep scores and status
            if (result is Resource.Success) {
                fetchTradeDetails(tradeId)
            }
        }
    }
}
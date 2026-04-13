package com.hugogarry.betterbarter.ui.circles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.Circle
import com.hugogarry.betterbarter.data.model.Trade
import com.hugogarry.betterbarter.data.repository.AuthRepository
import com.hugogarry.betterbarter.data.repository.CircleRepository
import com.hugogarry.betterbarter.data.repository.TradeRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Data class to hold the complete state for the screen
data class CircleDetailsUiState(
    val circle: Circle? = null,
    val availableTrades: List<Trade> = emptyList(),
    val isUserAdmin: Boolean = false,
    val isMember: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

class CircleDetailsViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val circleRepository = CircleRepository()
    private val tradeRepository = TradeRepository()
    private val authRepository: AuthRepository = AuthRepository()

    val circleId: String = savedStateHandle.get<String>("circleId")!!


    // Single StateFlow for the entire UI
    private val _uiState = MutableStateFlow(CircleDetailsUiState())
    val uiState: StateFlow<CircleDetailsUiState> = _uiState

    init {
        fetchScreenData()
    }

    fun fetchScreenData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Run all network calls in parallel for speed
            val circleDetailsDeferred = async { circleRepository.getCircleDetails(circleId) }
            val tradesDeferred = async { tradeRepository.getTradesForCircle(circleId) }
            val userProfileDeferred = async { authRepository.getProfile() }
            val myCirclesDeferred = async { circleRepository.getMyCircles() } // Verify membership explicitly

            val circleResult = circleDetailsDeferred.await()
            val tradesResult = tradesDeferred.await()
            val profileResult = userProfileDeferred.await()
            val myCirclesResult = myCirclesDeferred.await()

            val error = when {
                circleResult is Resource.Error -> circleResult.message
                tradesResult is Resource.Error -> tradesResult.message
                profileResult is Resource.Error -> profileResult.message
                myCirclesResult is Resource.Error -> myCirclesResult.message
                else -> null
            }

            if (error != null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = error)
                return@launch
            }

            val circle = (circleResult as Resource.Success).data!!
            val availableTrades = (tradesResult as Resource.Success).data ?: emptyList()
            val user = (profileResult as Resource.Success).data!!
            val myCircles = (myCirclesResult as Resource.Success).data ?: emptyList()

            // Robust membership check:
            // 1. Is user an admin?
            // 2. Does the API explicitly say they are a member?
            // 3. Is this circle ID present in the user's "My Circles" list?
            val isUserAdmin = circle.admins?.any { it.id == user.id } ?: false
            val isMember = isUserAdmin || circle.isMember || myCircles.any { it.id == circleId }

            _uiState.value = CircleDetailsUiState(
                circle = circle,
                availableTrades = availableTrades,
                isUserAdmin = isUserAdmin,
                isMember = isMember,
                isLoading = false
            )
        }
    }
}
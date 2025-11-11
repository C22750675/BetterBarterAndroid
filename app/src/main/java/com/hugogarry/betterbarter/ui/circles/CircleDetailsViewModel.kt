package com.hugogarry.betterbarter.ui.circles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.Circle
import com.hugogarry.betterbarter.data.model.Trade
import com.hugogarry.betterbarter.data.repository.AuthRepository
import com.hugogarry.betterbarter.data.repository.CircleRepository
import com.hugogarry.betterbarter.data.repository.ItemRepository
import com.hugogarry.betterbarter.data.repository.TradeRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Data class to hold the complete state for the screen
data class CircleDetailsUiState(
    val circle: Circle? = null,
    val activeTrades: List<Trade> = emptyList(),
    val isUserAdmin: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

class CircleDetailsViewModel(
    savedStateHandle: SavedStateHandle
    // Remove AuthRepository from the constructor
) : ViewModel() {

    private val circleRepository = CircleRepository()
    private val itemRepository = ItemRepository()
    private val tradeRepository = TradeRepository()
    // Initialize AuthRepository as a property inside the class
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

            // Run all network calls in parallel
            val circleDetailsDeferred = async { circleRepository.getCircleDetails(circleId) }
            // --- THIS IS THE FIX ---
            val tradesDeferred = async { tradeRepository.getTradesForCircle(circleId) }
            // --- END OF FIX ---
            val userProfileDeferred = async { authRepository.getProfile() }

            // Await the results
            val circleResult = circleDetailsDeferred.await()
            val tradesResult = tradesDeferred.await() // <-- Use new result
            val profileResult = userProfileDeferred.await()

            // Check for errors
            val error = if (circleResult is Resource.Error) circleResult.message
            else if (tradesResult is Resource.Error) tradesResult.message // <-- Check new result
            else if (profileResult is Resource.Error) profileResult.message
            else null

            if (error != null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = error)
                return@launch
            }

            // We can assume success if no errors
            val circle = (circleResult as Resource.Success).data!!
            val activeTrades = (tradesResult as Resource.Success).data ?: emptyList() // <-- Get trades
            val user = (profileResult as Resource.Success).data!!

            // --- Simplified Admin Check ---
            val adminIds = circle.admins?.map { it.id }?.toSet() ?: emptySet()
            val isUserAdmin = user.id in adminIds

            _uiState.value = CircleDetailsUiState(
                circle = circle,
                activeTrades = activeTrades, // <-- Pass the new list of Trades
                isUserAdmin = isUserAdmin,
                isLoading = false
            )
        }
    }
}
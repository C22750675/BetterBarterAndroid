package com.hugogarry.betterbarter.ui.trades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.TradeApplication
import com.hugogarry.betterbarter.data.repository.TradeRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Define specific results for actions
sealed class TradeActionResult {
    data class Accepted(val tradeId: String, val message: String) : TradeActionResult()
    data class Declined(val message: String) : TradeActionResult()
}

class TradeApplicationsViewModel(
    private val tradeRepository: TradeRepository = TradeRepository()
) : ViewModel() {

    private val _applications = MutableStateFlow<Resource<List<TradeApplication>>>(Resource.Idle())
    val applications: StateFlow<Resource<List<TradeApplication>>> = _applications

    // Changed from Resource<String> to Resource<TradeActionResult> for type-safe handling
    private val _actionState = MutableStateFlow<Resource<TradeActionResult>>(Resource.Idle())
    val actionState: StateFlow<Resource<TradeActionResult>> = _actionState

    fun fetchApplications(tradeId: String) {
        viewModelScope.launch {
            _applications.value = Resource.Loading()
            _applications.value = tradeRepository.getTradeApplications(tradeId)
        }
    }

    fun acceptApplication(application: TradeApplication) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading()
            val result = tradeRepository.acceptApplication(application.id)
            if (result is Resource.Success) {
                // Return structured data: The ID to navigate to, and a message
                _actionState.value = Resource.Success(
                    TradeActionResult.Accepted(application.tradeId, "Trade Application Accepted!")
                )
                // Refresh the list
                fetchApplications(application.tradeId)
            } else {
                _actionState.value = Resource.Error(result.message ?: "Failed to accept")
            }
        }
    }

    fun declineApplication(application: TradeApplication) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading()
            val result = tradeRepository.declineApplication(application.id)

            if (result is Resource.Success) {
                _actionState.value = Resource.Success(
                    TradeActionResult.Declined("Application Declined")
                )

                // Optimistic Update
                val currentState = _applications.value
                if (currentState is Resource.Success) {
                    val currentList = currentState.data ?: emptyList()
                    val updatedList = currentList.filter { it.id != application.id }
                    _applications.value = Resource.Success(updatedList)
                } else {
                    fetchApplications(application.tradeId)
                }
            } else {
                _actionState.value = Resource.Error(result.message ?: "Failed to decline")
            }
        }
    }

    fun clearActionState() {
        _actionState.value = Resource.Idle()
    }
}
package com.hugogarry.betterbarter.ui.trades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.TradeApplication
import com.hugogarry.betterbarter.data.repository.TradeRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TradeApplicationsViewModel(
    private val tradeRepository: TradeRepository = TradeRepository()
) : ViewModel() {

    private val _applications = MutableStateFlow<Resource<List<TradeApplication>>>(Resource.Idle())
    val applications: StateFlow<Resource<List<TradeApplication>>> = _applications

    // Add state for accept/decline actions
    private val _actionState = MutableStateFlow<Resource<String>>(Resource.Idle())
    val actionState: StateFlow<Resource<String>> = _actionState

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
                _actionState.value = Resource.Success("Application Accepted! Chat created.")
                // Refresh the list using the TRADE ID, not the application ID
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
                _actionState.value = Resource.Success("Application Declined")

                // Optimistic Update: Remove the item from the current local list immediately
                val currentState = _applications.value
                if (currentState is Resource.Success) {
                    val currentList = currentState.data ?: emptyList()
                    // Create a new list excluding the declined application
                    val updatedList = currentList.filter { it.id != application.id }
                    _applications.value = Resource.Success(updatedList)
                } else {
                    // Fallback: If local state isn't valid, fetch from server
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
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

    fun fetchApplications(tradeId: String) {
        viewModelScope.launch {
            _applications.value = Resource.Loading()
            _applications.value = tradeRepository.getTradeApplications(tradeId)
        }
    }

    fun acceptApplication(application: TradeApplication) {
        // TODO: Implement accept logic in next iteration
    }

    fun declineApplication(application: TradeApplication) {
        // TODO: Implement decline logic in next iteration
    }
}
package com.hugogarry.betterbarter.ui.trades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.CreateDisputeRequest
import com.hugogarry.betterbarter.data.remote.ApiClient
import com.hugogarry.betterbarter.data.repository.DisputeRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RaiseDisputeViewModel(
    private val disputeRepository: DisputeRepository = DisputeRepository(ApiClient.apiService)
) : ViewModel() {

    // Expecting Resource<Unit> instead of Resource<Dispute>
    private val _submitState = MutableStateFlow<Resource<Unit>>(Resource.Idle())
    val submitState: StateFlow<Resource<Unit>> = _submitState

    fun submitDispute(tradeId: String, description: String) {
        val trimmedDesc = description.trim()
        if (trimmedDesc.length < 10) {
            _submitState.value = Resource.Error("Please provide at least 10 characters describing the issue.")
            return
        }

        viewModelScope.launch {
            _submitState.value = Resource.Loading()
            val request = CreateDisputeRequest(tradeId, trimmedDesc)
            _submitState.value = disputeRepository.createDispute(request)
        }
    }
}
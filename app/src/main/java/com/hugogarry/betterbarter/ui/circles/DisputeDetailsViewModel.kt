package com.hugogarry.betterbarter.ui.circles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.Dispute
import com.hugogarry.betterbarter.data.model.DisputeSeverity
import com.hugogarry.betterbarter.data.model.ResolveDisputeRequest
import com.hugogarry.betterbarter.data.remote.ApiClient
import com.hugogarry.betterbarter.data.repository.DisputeRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DisputeDetailsViewModel(
    private val repository: DisputeRepository = DisputeRepository(ApiClient.apiService)
) : ViewModel() {

    private val _disputeDetails = MutableStateFlow<Resource<Dispute>>(Resource.Loading())
    val disputeDetails: StateFlow<Resource<Dispute>> = _disputeDetails

    private val _resolveResult = MutableStateFlow<Resource<Dispute>?>(null)
    val resolveResult: StateFlow<Resource<Dispute>?> = _resolveResult

    fun getDispute(id: String) {
        viewModelScope.launch {
            _disputeDetails.value = Resource.Loading()
            _disputeDetails.value = repository.getDisputeDetails(id)
        }
    }

    fun resolveDispute(id: String, culpritId: String, severity: DisputeSeverity, note: String) {
        viewModelScope.launch {
            _resolveResult.value = Resource.Loading()
            val request = ResolveDisputeRequest(culpritId, severity, note)
            _resolveResult.value = repository.resolveDispute(id, request)
        }
    }
}
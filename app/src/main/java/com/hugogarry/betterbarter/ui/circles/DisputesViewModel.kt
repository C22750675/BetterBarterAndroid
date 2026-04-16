package com.hugogarry.betterbarter.ui.circles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.Dispute
import com.hugogarry.betterbarter.data.remote.ApiClient
import com.hugogarry.betterbarter.data.repository.DisputeRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DisputesViewModel : ViewModel() {
    private val repository = DisputeRepository(ApiClient.apiService)

    private val _disputes = MutableStateFlow<Resource<List<Dispute>>>(Resource.Loading())
    val disputes: StateFlow<Resource<List<Dispute>>> = _disputes

    fun fetchDisputes(circleId: String?) {
        viewModelScope.launch {
            _disputes.value = Resource.Loading()
            _disputes.value = repository.getDisputes(circleId)
        }
    }
}
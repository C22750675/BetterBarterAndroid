package com.hugogarry.betterbarter.ui.circles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.Circle
import com.hugogarry.betterbarter.data.repository.CircleRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CirclesViewModel(
    private val circleRepository: CircleRepository = CircleRepository()
) : ViewModel() {

    private val _circlesState = MutableStateFlow<Resource<List<Circle>>>(Resource.Idle())
    val circlesState: StateFlow<Resource<List<Circle>>> = _circlesState

    init {
        fetchMyCircles()
    }

    fun fetchMyCircles() {
        viewModelScope.launch {
            _circlesState.value = Resource.Loading()
            _circlesState.value = circleRepository.getMyCircles()
        }
    }
}
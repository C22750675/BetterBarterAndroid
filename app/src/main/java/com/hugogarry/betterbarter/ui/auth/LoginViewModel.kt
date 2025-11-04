package com.hugogarry.betterbarter.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.LoginRequest
import com.hugogarry.betterbarter.data.model.LoginResponse
import com.hugogarry.betterbarter.data.repository.AuthRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<LoginResponse>>(Resource.Idle())
    val loginState: StateFlow<Resource<LoginResponse>> = _loginState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            val request = LoginRequest(username, password)
            _loginState.value = authRepository.loginUser(request)
        }
    }
}
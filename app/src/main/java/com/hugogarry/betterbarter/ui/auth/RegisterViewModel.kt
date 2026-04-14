package com.hugogarry.betterbarter.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.LoginResponse
import com.hugogarry.betterbarter.data.model.RegisterRequest
import com.hugogarry.betterbarter.data.remote.ApiClient
import com.hugogarry.betterbarter.data.repository.AuthRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(private val authRepository: AuthRepository = AuthRepository(ApiClient.apiService)) : ViewModel() {

    private val _registerState = MutableStateFlow<Resource<LoginResponse>>(Resource.Idle())
    val registerState: StateFlow<Resource<LoginResponse>> = _registerState

    fun register(username: String, password: String, passwordConfirm: String) {
        if (username.contains(" ")) {
            _registerState.value = Resource.Error("Username cannot contain spaces.")
            return
        }
        if (password != passwordConfirm) {
            _registerState.value = Resource.Error("Passwords do not match.")
            return
        }
        if (!isPasswordStrong(password)) {
            val errorMessage = "Password must be at least 8 characters long and contain an uppercase letter, a lowercase letter, a number, and a special character."
            _registerState.value = Resource.Error(errorMessage)
            return
        }

        viewModelScope.launch {
            _registerState.value = Resource.Loading()
            val request = RegisterRequest(username, password)
            _registerState.value = authRepository.registerUser(request)
        }
    }

    private fun isPasswordStrong(password: String): Boolean {
        // Regex for: at least 8 chars, 1 uppercase, 1 lowercase, 1 number, 1 special char
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
        val pattern = Regex(passwordPattern)
        return pattern.matches(password)
    }
}
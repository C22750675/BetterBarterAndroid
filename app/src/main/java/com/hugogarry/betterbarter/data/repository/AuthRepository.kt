package com.hugogarry.betterbarter.data.repository

import com.hugogarry.betterbarter.data.model.LoginRequest
import com.hugogarry.betterbarter.data.model.LoginResponse
import com.hugogarry.betterbarter.data.model.RegisterRequest
import com.hugogarry.betterbarter.data.model.User
import com.hugogarry.betterbarter.data.remote.ApiClient
import com.hugogarry.betterbarter.data.remote.ApiService
import com.hugogarry.betterbarter.util.Resource
import java.io.IOException
import com.hugogarry.betterbarter.util.SessionManager
import com.hugogarry.betterbarter.data.model.UpdateProfileRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val apiService: ApiService = ApiClient.apiService) {

    suspend fun registerUser(registerRequest: RegisterRequest): Resource<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.registerUser(registerRequest)
            if (response.isSuccessful && response.body() != null) {
                // Save the token on successful registration, just like in login
                val loginResponse = response.body()!!
                SessionManager.saveToken(loginResponse.accessToken)
                Resource.Success(loginResponse)
            } else {
                // Handle specific HTTP errors based on response code
                when (response.code()) {
                    409 -> Resource.Error("This username is already taken.") // 409 Conflict
                    400 -> Resource.Error("Invalid username or password format.") // 400 Bad Request
                    else -> Resource.Error("An unexpected error occurred: ${response.message()}")
                }
            }
        } catch (e: IOException) {
            Resource.Error("Could not reach the server. Please check your internet connection.")
        } catch (e: Exception) {
            Resource.Error("An unexpected error occurred: ${e.message}")
        }
    }

    suspend fun loginUser(loginRequest: LoginRequest): Resource<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.loginUser(loginRequest)
            if (response.isSuccessful && response.body() != null) {
                // Save the token to the session manager on successful login
                val loginResponse = response.body()!!
                SessionManager.saveToken(loginResponse.accessToken)
                Resource.Success(loginResponse)
            } else {
                if (response.code() == 401) { // Unauthorized
                    Resource.Error("Invalid username or password.")
                } else {
                    Resource.Error("An unexpected error occurred: ${response.message()}")
                }
            }
        } catch (e: IOException) {
            Resource.Error("Could not reach the server. Please check your internet connection.")
        } catch (e: Exception) {
            Resource.Error("An unexpected error occurred: ${e.message}")
        }
    }

    suspend fun getProfile(): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to fetch profile: ${response.message()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: Could not fetch profile.")
        } catch (e: Exception) {
            Resource.Error("Failed to fetch profile: ${e.message}")
        }
    }

    suspend fun updateProfile(updateProfileDto: UpdateProfileRequest): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateProfile(updateProfileDto)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to update profile: ${response.message()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: Could not update profile.")
        } catch (e: Exception) {
            Resource.Error("Failed to update profile: ${e.message}")
        }
    }

    /**
     * Logs the user out by clearing their session token.
     */
    fun logout() {
        SessionManager.clearToken()
    }
}
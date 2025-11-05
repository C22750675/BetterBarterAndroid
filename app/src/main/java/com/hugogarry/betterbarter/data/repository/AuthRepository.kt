package com.hugogarry.betterbarter.data.repository

import com.hugogarry.betterbarter.data.model.LoginRequest
import com.hugogarry.betterbarter.data.model.LoginResponse
import com.hugogarry.betterbarter.data.model.RegisterRequest
import com.hugogarry.betterbarter.data.model.User
import com.hugogarry.betterbarter.data.remote.ApiClient
import com.hugogarry.betterbarter.data.remote.ApiService
import com.hugogarry.betterbarter.util.Resource
import java.io.IOException
import retrofit2.HttpException
import com.hugogarry.betterbarter.util.SessionManager
import com.hugogarry.betterbarter.data.model.UpdateProfileRequest

class AuthRepository(private val apiService: ApiService = ApiClient.apiService) {

    suspend fun registerUser(registerRequest: RegisterRequest): Resource<LoginResponse> {
        return try {
            val response = apiService.registerUser(registerRequest)
            // Save the token on successful registration, just like in login
            SessionManager.saveToken(response.accessToken)
            Resource.Success(response)
        } catch (e: IOException) {
            Resource.Error("Could not reach the server. Please check your internet connection.")
        } catch (e: HttpException) {
            // Handle specific HTTP errors
            when (e.code()) {
                409 -> Resource.Error("This username is already taken.") // 409 Conflict
                400 -> Resource.Error("Invalid username or password format.") // 400 Bad Request
                else -> Resource.Error("An unexpected error occurred: ${e.message()}")
            }
        }
    }

    suspend fun loginUser(loginRequest: LoginRequest): Resource<LoginResponse> {
        return try {
            val response = apiService.loginUser(loginRequest)
            // Save the token to the session manager on successful login
            SessionManager.saveToken(response.accessToken)
            Resource.Success(response)
        } catch (e: IOException) {
            Resource.Error("Could not reach the server. Please check your internet connection.")
        } catch (e: HttpException) {
            if (e.code() == 401) { // Unauthorized
                Resource.Error("Invalid username or password.")
            } else {
                Resource.Error("An unexpected error occurred: ${e.message()}")
            }
        }
    }

    suspend fun getProfile(): Resource<User> {
        return try {
            Resource.Success(apiService.getProfile())
        } catch (e: Exception) {
            Resource.Error("Failed to fetch profile: ${e.message}")
        }
    }

    suspend fun updateProfile(updateProfileDto: UpdateProfileRequest): Resource<User> {
        return try {
            val user = apiService.updateProfile(updateProfileDto)
            Resource.Success(user)
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
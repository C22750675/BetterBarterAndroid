package com.hugogarry.betterbarter.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SessionManager {

    private const val PREFS_NAME = "better_barter_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_SERVER_URL = "server_url"

    // Default to the Android Emulator loopback address
    private const val DEFAULT_URL = "http://10.0.2.2:3000/api/"

    private lateinit var sharedPreferences: SharedPreferences

    // A flow to broadcast session expiry events
    private val _sessionExpired = MutableStateFlow(false)
    val sessionExpired: StateFlow<Boolean> = _sessionExpired

    /**
     * Must be called once from the Application class to initialize.
     */
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Saves the access token to SharedPreferences.
     */
    fun saveToken(token: String) {
        sharedPreferences.edit {
            putString(KEY_ACCESS_TOKEN, token)
        }
    }

    /**
     * Retrieves the access token from SharedPreferences.
     * @return The saved token, or null if it doesn't exist.
     */
    fun getToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    /**
     * Clears the access token, effectively logging the user out.
     */
    fun clearToken() {
        sharedPreferences.edit {
            remove(KEY_ACCESS_TOKEN)
        }
    }

    /**
     * Saves the custom server URL.
     * Ensures it ends with a slash for consistency.
     */
    fun saveServerUrl(url: String) {
        val formattedUrl = if (url.endsWith("/")) url else "$url/"
        sharedPreferences.edit {
            putString(KEY_SERVER_URL, formattedUrl)
        }
    }

    /**
     * Retrieves the currently configured server URL.
     */
    fun getServerUrl(): String {
        return sharedPreferences.getString(KEY_SERVER_URL, DEFAULT_URL) ?: DEFAULT_URL
    }

    /**
     * Notifies observers that the session has expired.
     */
    fun notifySessionExpired() {
        // Clear the token first
        clearToken()
        // Then notify observers
        _sessionExpired.value = true
    }

    /**
     * Resets the expiry flag, e.g., after the UI has handled the navigation.
     */
    fun clearSessionExpiredFlag() {
        _sessionExpired.value = false
    }
}
// In: util/SessionManager.kt
package com.hugogarry.betterbarter.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SessionManager {

    private const val PREFS_NAME = "better_barter_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"

    private lateinit var sharedPreferences: SharedPreferences

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
        val editor = sharedPreferences.edit()
        editor.putString(KEY_ACCESS_TOKEN, token)
        editor.apply() // Use apply() for asynchronous saving
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
}
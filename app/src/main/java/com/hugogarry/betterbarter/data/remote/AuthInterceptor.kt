package com.hugogarry.betterbarter.data.remote

import com.hugogarry.betterbarter.util.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // Get the original request from the chain
        val originalRequest = chain.request()

        // Get the token from the SessionManager
        val token = SessionManager.getToken()

        // If the token exists, add the Authorization header
        val requestBuilder = originalRequest.newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val newRequest = requestBuilder.build()

        // Proceed with the request
        val response = chain.proceed(newRequest)

        // Check if the response code is 401 (Unauthorized)
        // We also check that this wasn't a 401 from the login page itself,
        // as that just means "wrong password" and shouldn't log the user out.
        val isLoginRequest = originalRequest.url.encodedPath.endsWith("/auth/login")

        if (response.code == 401 && !isLoginRequest) {
            // This is a "session expired" error.
            // Notify the SessionManager.
            SessionManager.notifySessionExpired()
        }

        // Return the response, even if it's a 401.
        return response
    }
}
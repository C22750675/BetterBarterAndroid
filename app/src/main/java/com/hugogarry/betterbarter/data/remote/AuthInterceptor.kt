// In: data/remote/AuthInterceptor.kt
package com.hugogarry.betterbarter.data.remote

import com.hugogarry.betterbarter.util.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // Get the original request from the chain
        val originalRequest = chain.request()

        // Get the token from our SessionManager
        val token = SessionManager.getToken()

        // If the token exists, add the Authorization header
        val requestBuilder = originalRequest.newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val newRequest = requestBuilder.build()
        // Proceed with the new (or original) request
        return chain.proceed(newRequest)
    }
}
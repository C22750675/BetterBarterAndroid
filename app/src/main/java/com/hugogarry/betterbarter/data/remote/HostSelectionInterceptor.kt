package com.hugogarry.betterbarter.data.remote

import com.hugogarry.betterbarter.util.SessionManager
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * An Interceptor that dynamically swaps the host and port of the request
 * based on the URL stored in SessionManager.
 */
class HostSelectionInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val dynamicUrlString = SessionManager.getServerUrl()
        val dynamicUrl = dynamicUrlString.toHttpUrlOrNull()

        if (dynamicUrl != null) {
            val newUrl = request.url.newBuilder()
                .scheme(dynamicUrl.scheme)
                .host(dynamicUrl.host)
                .port(dynamicUrl.port)
                .build()

            request = request.newBuilder()
                .url(newUrl)
                .build()
        }

        return chain.proceed(request)
    }
}
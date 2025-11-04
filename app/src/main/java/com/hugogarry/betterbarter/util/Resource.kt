package com.hugogarry.betterbarter.util

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    /**
     * Represents the initial, idle state before any operation has started.
     */
    class Idle<T> : Resource<T>()

    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T> : Resource<T>()
}
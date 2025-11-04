package com.hugogarry.betterbarter

import android.app.Application
import com.hugogarry.betterbarter.util.SessionManager

class BetterBarterApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize the SessionManager with the application context
        SessionManager.init(this)
    }
}
package com.hugogarry.betterbarter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.hugogarry.betterbarter.util.SessionManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // The NavHostFragment is already set up via XML.
        // Now, we add the logic to decide where to go on startup.

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Check if the user is already logged in
        if (SessionManager.getToken().isNullOrBlank()) {
            // User is not logged in, the graph will start at welcomeFragment (default)
            // No action needed.
        } else {
            // User IS logged in. Navigate to the main app screen and clear the back stack.
            navController.navigate(R.id.itemListFragment, null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
            )
        }
    }
}
package com.hugogarry.betterbarter.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.util.SessionManager

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        bottomNav = findViewById(R.id.bottom_navigation)

        // Connect the BottomNavigationView to the NavController
        bottomNav.setupWithNavController(navController)

        // Add logic to show/hide the bottom nav bar
        setupBottomNavVisibility()

        // Handle startup navigation
        if (savedInstanceState == null) {
            handleStartupNavigation()
        }
    }

    private fun setupBottomNavVisibility() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Define the set of top-level destinations where the nav bar should be visible
            val topLevelDestinations = setOf(
                R.id.mapFragment,
                R.id.circlesFragment,
                R.id.tradesFragment,
                R.id.profileFragment
            )
            if (destination.id in topLevelDestinations) {
                bottomNav.visibility = View.VISIBLE
            } else {
                bottomNav.visibility = View.GONE
            }
        }
    }

    private fun handleStartupNavigation() {
        if (SessionManager.getToken().isNullOrBlank()) {
            // User is not logged in, remain on the auth flow (the default)
            navController.navigate(R.id.auth_flow)
        } else {
            // User IS logged in, navigate to the main app flow
            navController.navigate(R.id.main_flow)
        }
    }
}
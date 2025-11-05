package com.hugogarry.betterbarter.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.NavOptions // REQUIRED IMPORT
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.util.SessionManager

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Make the app layout fullscreen to draw behind system bars
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        navHostFragment = supportFragmentManager
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

        // Call the new function to handle insets
        setupWindowInsets()
    }

    // NEW FUNCTION: Handles window insets for edge-to-edge display
    private fun setupWindowInsets() {
        val rootLayout = findViewById<ConstraintLayout>(R.id.main_activity_root)

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Apply padding to the top of the NavHostFragment (for the status bar)
            navHostFragment.view?.updatePadding(top = insets.top)

            // Apply padding to the bottom of the BottomNavigationView (for the gesture nav bar)
            bottomNav.updatePadding(bottom = insets.bottom)

            // Return default insets to let children handle their own if needed
            WindowInsetsCompat.CONSUMED
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
            // No navigation needed, as it's the start destination
        } else {
            // User IS logged in, navigate to the main app flow

            // REQUIRED FIX:
            // Create NavOptions to pop the auth_flow (inclusive) from the back stack.
            // This ensures pressing "back" on the main screen exits the app.
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.auth_flow, true)
                .build()

            navController.navigate(R.id.main_flow, null, navOptions)
        }
    }
}
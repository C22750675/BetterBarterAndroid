package com.hugogarry.betterbarter.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.util.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var navHostFragment: NavHostFragment
    private var isBottomNavVisible = true

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

        // Call the function to handle insets
        setupWindowInsets()

        // Start observing for session expiry events
        observeSessionExpiry()
    }

    private fun observeSessionExpiry() {
        lifecycleScope.launch {
            SessionManager.sessionExpired.collectLatest { hasExpired ->
                if (hasExpired) {
                    // Show a message to the user
                    Toast.makeText(
                        this@MainActivity,
                        "Your session has expired. Please log in again.",
                        Toast.LENGTH_LONG
                    ).show()

                    // Reset the flag in the SessionManager
                    SessionManager.clearSessionExpiredFlag()

                    // Define navigation options to clear the entire back stack
                    // (all fragments in main_flow)
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.main_flow, true)
                        .build()

                    // Navigate back to the auth_flow (which leads to WelcomeFragment)
                    navController.navigate(R.id.auth_flow, null, navOptions)
                }
            }
        }
    }

    // Handles window insets for edge-to-edge display
    private fun setupWindowInsets() {
        val rootLayout = findViewById<ConstraintLayout>(R.id.main_activity_root)

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { _, windowInsets ->
            val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

            // Apply padding to the top of the NavHostFragment (for the status bar)
            navHostFragment.view?.updatePadding(top = systemBarsInsets.top)

            if (isBottomNavVisible) {
                // BottomNav is visible: Give it the bottom padding and clear the NavHost padding
                bottomNav.updatePadding(bottom = systemBarsInsets.bottom)
                navHostFragment.view?.updatePadding(bottom = 0)
            } else {
                // BottomNav is hidden: The NavHost needs the bottom padding (and keyboard inset if visible)
                val bottomPadding = maxOf(systemBarsInsets.bottom, imeInsets.bottom)
                navHostFragment.view?.updatePadding(bottom = bottomPadding)
                // Clear padding from hidden bottom nav just to be safe
                bottomNav.updatePadding(bottom = 0)
            }

            // Return consumed so the root layout doesn't apply the insets globally and mess up our specific layout
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
                R.id.myChatsFragment,
                R.id.profileFragment
            )
            val rootLayout = findViewById<ConstraintLayout>(R.id.main_activity_root)

            if (destination.id in topLevelDestinations) {
                bottomNav.visibility = View.VISIBLE
                isBottomNavVisible = true
            } else {
                bottomNav.visibility = View.GONE
                isBottomNavVisible = false
            }

            // Request an inset layout pass whenever visibility changes
            ViewCompat.requestApplyInsets(rootLayout)
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
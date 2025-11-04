package com.hugogarry.betterbarter.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hugogarry.betterbarter.R

/**
 * The first screen a logged-out user sees.
 * It provides options to either register for a new account or log in to an existing one.
 */
class WelcomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the buttons from the layout
        val createAccountButton = view.findViewById<Button>(R.id.buttonCreateAccount)
        val loginButton = view.findViewById<Button>(R.id.buttonGoToLogin)

        // Set up the click listener for the "Create Account" button
        createAccountButton.setOnClickListener {
            // Use the NavController to navigate to the RegisterFragment
            // The action ID is defined in the nav_graph.xml
            findNavController().navigate(R.id.action_welcomeFragment_to_registerFragment)
        }

        // Set up the click listener for the "I Already Have an Account" button
        loginButton.setOnClickListener {
            // Use the NavController to navigate to the LoginFragment
            // The action ID is defined in the nav_graph.xml
            findNavController().navigate(R.id.action_welcomeFragment_to_loginFragment)
        }
    }
}
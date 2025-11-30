package com.hugogarry.betterbarter.ui.auth

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.util.SessionManager

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
        val changeServerButton = view.findViewById<Button>(R.id.buttonChangeServer)

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

        changeServerButton.setOnClickListener {
            showServerConfigurationDialog()
        }
    }

    private fun showServerConfigurationDialog() {
        val input = EditText(context)
        input.hint = "http://192.168.1.X:3000/api/"
        // Pre-fill with current value
        input.setText(SessionManager.getServerUrl())

        AlertDialog.Builder(context)
            .setTitle("Set Server URL")
            .setMessage("Enter the full API URL (e.g., http://192.168.1.50:3000/api/)")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newUrl = input.text.toString().trim()
                if (newUrl.isNotBlank()) {
                    SessionManager.saveServerUrl(newUrl)
                    Toast.makeText(context, "Server URL updated", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
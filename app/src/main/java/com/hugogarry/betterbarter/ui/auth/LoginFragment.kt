// In: ui/auth/LoginFragment.kt
package com.hugogarry.betterbarter.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameEditText = view.findViewById<EditText>(R.id.editTextUsername)
        val passwordEditText = view.findViewById<EditText>(R.id.editTextPassword)
        val loginButton = view.findViewById<Button>(R.id.buttonLogin)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBarLogin)
        val errorTextView = view.findViewById<TextView>(R.id.textViewLoginError)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(username, password)
            } else {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        observeLoginState(progressBar, errorTextView)
    }

    // In: ui/auth/LoginFragment.kt

    private fun observeLoginState(progressBar: ProgressBar, errorTextView: TextView) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginState.collectLatest { resource ->
                // These bindings correctly handle the visibility for Idle, Success, Error, and Loading
                progressBar.isVisible = resource is Resource.Loading
                errorTextView.isVisible = resource is Resource.Error

                when (resource) {
                    is Resource.Success -> {
                        if (resource.data != null) {
                            Toast.makeText(context, "Login Successful!", Toast.LENGTH_LONG).show()
                            // TODO: Navigate to the main part of the app
                            // e.g., (activity as MainActivity).navigateToMainScreen()
                        }
                    }
                    is Resource.Error -> {
                        errorTextView.text = resource.message
                    }
                    is Resource.Loading -> {
                        // Handled by the isVisible binding above, nothing needed here.
                    }
                    // ADD THIS BRANCH TO MAKE THE 'WHEN' EXHAUSTIVE
                    is Resource.Idle -> {
                        // This is the initial state before the user has tried to log in.
                        // The UI is already in its default state, so we don't need to do anything.
                    }
                }
            }
        }
    }
}
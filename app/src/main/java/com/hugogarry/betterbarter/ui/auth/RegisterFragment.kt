package com.hugogarry.betterbarter.ui.auth

import android.os.Bundle
import android.text.InputFilter
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
import androidx.navigation.fragment.findNavController
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameEditText = view.findViewById<EditText>(R.id.editTextUsernameRegister)
        val passwordEditText = view.findViewById<EditText>(R.id.editTextPasswordRegister)
        val passwordConfirmEditText = view.findViewById<EditText>(R.id.editTextPasswordConfirm)
        val registerButton = view.findViewById<Button>(R.id.buttonRegister)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBarRegister)
        val errorTextView = view.findViewById<TextView>(R.id.textViewRegisterError)

        val noSpaceFilter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (Character.isWhitespace(source[i])) {
                    return@InputFilter "" // Return empty string to ignore the space
                }
            }
            null // Keep the original character
        }
        usernameEditText.filters = arrayOf(noSpaceFilter)

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val passwordConfirm = passwordConfirmEditText.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty() && passwordConfirm.isNotEmpty()) {
                viewModel.register(username, password, passwordConfirm)
            } else {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        observeRegisterState(progressBar, errorTextView)
    }

    private fun observeRegisterState(progressBar: ProgressBar, errorTextView: TextView) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.registerState.collectLatest { resource ->
                progressBar.isVisible = resource is Resource.Loading
                errorTextView.isVisible = resource is Resource.Error

                when (resource) {
                    is Resource.Success -> {
                        Toast.makeText(context, "Registration Successful!", Toast.LENGTH_LONG).show()
                        findNavController().navigate(R.id.action_auth_to_main)
                    }
                    is Resource.Error -> {
                        errorTextView.text = resource.message
                    }
                    is Resource.Loading, is Resource.Idle -> {
                        // UI state is handled by the visibility bindings above
                    }
                }
            }
        }
    }
}
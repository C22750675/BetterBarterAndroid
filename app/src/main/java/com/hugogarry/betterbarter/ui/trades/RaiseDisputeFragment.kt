package com.hugogarry.betterbarter.ui.trades

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
class RaiseDisputeFragment : Fragment() {

    private val viewModel: RaiseDisputeViewModel by viewModels()
    private val args: RaiseDisputeFragmentArgs by navArgs()

    private lateinit var descriptionEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_raise_dispute, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbarRaiseDispute)
        NavigationUI.setupWithNavController(toolbar, findNavController())

        descriptionEditText = view.findViewById(R.id.editTextDisputeDescription)
        submitButton = view.findViewById(R.id.buttonSubmitDispute)
        progressBar = view.findViewById(R.id.progressBarDispute)
        errorTextView = view.findViewById(R.id.textViewErrorDispute)

        submitButton.setOnClickListener {
            viewModel.submitDispute(args.tradeId, descriptionEditText.text.toString())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.submitState.collectLatest { resource ->
                progressBar.isVisible = resource is Resource.Loading
                submitButton.isEnabled = resource !is Resource.Loading
                errorTextView.isVisible = resource is Resource.Error

                when (resource) {
                    is Resource.Success -> {
                        Toast.makeText(context, "Dispute raised successfully.", Toast.LENGTH_LONG).show()
                        findNavController().popBackStack()
                    }
                    is Resource.Error -> {
                        errorTextView.text = resource.message
                    }
                    else -> {}
                }
            }
        }
    }
}
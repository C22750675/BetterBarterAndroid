package com.hugogarry.betterbarter.ui.circles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputLayout
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.data.model.Dispute
import com.hugogarry.betterbarter.data.model.DisputeSeverity
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DisputeDetailsFragment : Fragment() {

    private val viewModel: DisputeDetailsViewModel by viewModels()
    private val args: DisputeDetailsFragmentArgs by navArgs()

    private var currentDispute: Dispute? = null

    private lateinit var textReason: TextView
    private lateinit var radioInitiator: RadioButton
    private lateinit var radioRespondent: RadioButton
    private lateinit var radioGroup: RadioGroup
    private lateinit var spinnerSeverity: Spinner
    private lateinit var editNote: EditText
    private lateinit var inputLayoutNote: TextInputLayout
    private lateinit var errorTextView: TextView
    private lateinit var btnResolve: Button
    private lateinit var progressBar: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dispute_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbarDisputeDetails)
        NavigationUI.setupWithNavController(toolbar, findNavController())

        textReason = view.findViewById(R.id.textDisputeReason)
        val btnGoToChat = view.findViewById<Button>(R.id.btnGoToChat)
        radioInitiator = view.findViewById(R.id.radioInitiator)
        radioRespondent = view.findViewById(R.id.radioRespondent)
        radioGroup = view.findViewById(R.id.radioGroupCulprit)
        spinnerSeverity = view.findViewById(R.id.spinnerSeverity)
        editNote = view.findViewById(R.id.editResolutionNote)
        inputLayoutNote = view.findViewById(R.id.textInputLayoutNote)
        errorTextView = view.findViewById(R.id.textViewErrorDetails)
        btnResolve = view.findViewById(R.id.btnResolve)
        progressBar = view.findViewById(R.id.progressBarDetails)

        val severityOptions = DisputeSeverity.entries.map {
            it.name.lowercase().replaceFirstChar { c -> c.uppercase() }
        }
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, severityOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSeverity.adapter = spinnerAdapter

        viewModel.getDispute(args.disputeId)

        observeViewModel()

        btnGoToChat.setOnClickListener {
            currentDispute?.tradeId?.let { tradeId ->
                val action = DisputeDetailsFragmentDirections.actionDisputeDetailsFragmentToChatFragment(tradeId)
                findNavController().navigate(action)
            }
        }

        btnResolve.setOnClickListener {
            if (validateForm()) {
                val isProposerReporter = currentDispute?.trade?.proposerId == currentDispute?.reporterId
                val reporterId = currentDispute?.reporterId
                val otherPartyId = if (isProposerReporter) currentDispute?.trade?.recipientId else currentDispute?.trade?.proposerId

                val culpritId = if (radioInitiator.isChecked) reporterId else otherPartyId
                val selectedSeverity = DisputeSeverity.entries[spinnerSeverity.selectedItemPosition]
                val note = editNote.text.toString().trim()

                if (culpritId != null && currentDispute != null) {
                    viewModel.resolveDispute(currentDispute!!.id, culpritId, selectedSeverity, note)
                }
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.disputeDetails.collectLatest { resource ->
                progressBar.isVisible = resource is Resource.Loading

                if (resource is Resource.Success) {
                    // FIX: Safely handle the nullable data from the resource
                    resource.data?.let { data ->
                        currentDispute = data
                        bindDisputeData(data)
                    }
                } else if (resource is Resource.Error) {
                    errorTextView.text = resource.message
                    errorTextView.isVisible = true
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.resolveResult.collectLatest { resource ->
                progressBar.isVisible = resource is Resource.Loading
                btnResolve.isEnabled = resource !is Resource.Loading

                when (resource) {
                    is Resource.Success -> {
                        Toast.makeText(context, "Dispute resolved successfully.", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is Resource.Error -> {
                        errorTextView.text = resource.message
                        errorTextView.isVisible = true
                    }
                    else -> {}
                }
            }
        }
    }

    private fun bindDisputeData(dispute: Dispute) {
        textReason.text = dispute.description

        val isProposerReporter = dispute.trade?.proposerId == dispute.reporterId
        val reporterName = if (isProposerReporter) dispute.trade.proposer?.username else dispute.trade?.recipient?.username
        val otherPartyName = if (isProposerReporter) dispute.trade.recipient?.username else dispute.trade?.proposer?.username

        radioInitiator.text = "Reporter: ${reporterName ?: "Unknown"}"
        radioRespondent.text = "Other Party: ${otherPartyName ?: "Unknown"}"

        // Handle Resolved State
        if (dispute.status.lowercase() == "resolved") {
            disableFormForResolvedDispute(dispute)
        }
    }

    private fun disableFormForResolvedDispute(dispute: Dispute) {
        // Disable all inputs
        radioInitiator.isEnabled = false
        radioRespondent.isEnabled = false
        spinnerSeverity.isEnabled = false
        editNote.isEnabled = false

        // Hide the action button
        btnResolve.isVisible = false

        val reporterId = dispute.reporterId

        if (dispute.culpritId == reporterId) {
            radioInitiator.isChecked = true
        } else {
            radioRespondent.isChecked = true
        }

        // Set spinner to correct severity
        dispute.severity?.let { severityStr ->
            val index = DisputeSeverity.entries.toTypedArray().indexOfFirst {
                it.name.equals(severityStr, ignoreCase = true)
            }
            if (index != -1) spinnerSeverity.setSelection(index)
        }

        // Update error text view to act as a "Resolution Status" banner
        errorTextView.text = "This dispute was resolved on ${dispute.resolvedAt?.take(10) ?: "Date Unknown"}"
        errorTextView.setTextColor(requireContext().getColor(android.R.color.darker_gray))
        errorTextView.isVisible = true

        inputLayoutNote.hint = "Resolution Note (Read Only)"
    }

    private fun validateForm(): Boolean {
        var isValid = true
        errorTextView.isVisible = false
        inputLayoutNote.error = null

        if (radioGroup.checkedRadioButtonId == -1) {
            errorTextView.text = "Please select the party at fault."
            errorTextView.isVisible = true
            isValid = false
        }

        if (editNote.text.toString().trim().isEmpty()) {
            inputLayoutNote.error = "Internal resolution note is required."
            isValid = false
        }

        return isValid
    }
}
package com.hugogarry.betterbarter.ui.circles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import com.google.android.material.appbar.MaterialToolbar
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.data.model.Dispute
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DisputeDetailsFragment : Fragment() {

    private val viewModel: DisputeDetailsViewModel by viewModels()
    private val args: DisputeDetailsFragmentArgs by navArgs()

    private var currentDispute: Dispute? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dispute_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbarDisputeDetails)
        NavigationUI.setupWithNavController(toolbar, findNavController())

        val textReason = view.findViewById<TextView>(R.id.textDisputeReason)
        val btnGoToChat = view.findViewById<Button>(R.id.btnGoToChat)
        val radioInitiator = view.findViewById<RadioButton>(R.id.radioInitiator)
        val radioRespondent = view.findViewById<RadioButton>(R.id.radioRespondent)
        val spinnerSeverity = view.findViewById<Spinner>(R.id.spinnerSeverity)
        val editNote = view.findViewById<EditText>(R.id.editResolutionNote)
        val btnResolve = view.findViewById<Button>(R.id.btnResolve)

        // Fetch the dispute details using the nav argument
        viewModel.getDispute(args.disputeId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.disputeDetails.collectLatest { resource ->
                if (resource is Resource.Success) {
                    currentDispute = resource.data
                    textReason.text = currentDispute?.description

                    // Determine reporter identity dynamically from the nested trade object
                    val isProposerReporter = currentDispute?.trade?.proposerId == currentDispute?.reporterId

                    val reporterName = if (isProposerReporter) currentDispute?.trade?.proposer?.username else currentDispute?.trade?.recipient?.username
                    val otherPartyName = if (isProposerReporter) currentDispute?.trade?.recipient?.username else currentDispute?.trade?.proposer?.username

                    radioInitiator.text = "Reporter: ${reporterName ?: "Unknown"}"
                    radioRespondent.text = "Other Party: ${otherPartyName ?: "Unknown"}"
                } else if (resource is Resource.Error) {
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnGoToChat.setOnClickListener {
            currentDispute?.tradeId?.let { tradeId ->
                val action = DisputeDetailsFragmentDirections.actionDisputeDetailsFragmentToChatFragment(tradeId)
                findNavController().navigate(action)
            }
        }

        btnResolve.setOnClickListener {
            val isProposerReporter = currentDispute?.trade?.proposerId == currentDispute?.reporterId

            val reporterId = currentDispute?.reporterId
            val otherPartyId = if (isProposerReporter) currentDispute?.trade?.recipientId else currentDispute?.trade?.proposerId

            val culpritId = if (radioInitiator.isChecked) reporterId else otherPartyId
            val severity = spinnerSeverity.selectedItem.toString().uppercase()
            val note = editNote.text.toString()

            if (culpritId != null && currentDispute != null) {
                viewModel.resolveDispute(currentDispute!!.id, culpritId, severity, note)
            } else {
                Toast.makeText(context, "Please select a culprit.", Toast.LENGTH_SHORT).show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.resolveResult.collectLatest { resource ->
                if (resource is Resource.Success) {
                    Toast.makeText(context, "Dispute Resolved!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else if (resource is Resource.Error) {
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
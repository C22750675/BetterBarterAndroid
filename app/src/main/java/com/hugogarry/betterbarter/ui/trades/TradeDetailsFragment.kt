package com.hugogarry.betterbarter.ui.trades

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
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
import coil.load
import coil.transform.CircleCropTransformation
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.data.model.Trade
import com.hugogarry.betterbarter.data.model.TradeStatus
import com.hugogarry.betterbarter.util.Resource
import com.hugogarry.betterbarter.util.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject

class TradeDetailsFragment : Fragment() {

    private val viewModel: TradeDetailsViewModel by viewModels()
    private val args: TradeDetailsFragmentArgs by navArgs()

    private lateinit var itemImageView: ImageView
    private lateinit var itemNameTextView: TextView
    private lateinit var itemQuantityTextView: TextView
    private lateinit var itemDescriptionTextView: TextView

    private lateinit var proposerImageView: ImageView
    private lateinit var proposerNameTextView: TextView
    private lateinit var proposerReputationTextView: TextView

    private lateinit var tradeDescriptionTextView: TextView
    private lateinit var actionButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trade_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbarTradeDetails)
        NavigationUI.setupWithNavController(toolbar, findNavController())

        // Bind views
        itemImageView = view.findViewById(R.id.imageViewItemDetails)
        itemNameTextView = view.findViewById(R.id.textViewItemNameDetails)
        itemQuantityTextView = view.findViewById(R.id.textViewItemQuantityDetails)
        itemDescriptionTextView = view.findViewById(R.id.textViewItemDescriptionDetails)

        proposerImageView = view.findViewById(R.id.imageViewProposerProfile)
        proposerNameTextView = view.findViewById(R.id.textViewProposerName)
        proposerReputationTextView = view.findViewById(R.id.textViewProposerReputation)

        tradeDescriptionTextView = view.findViewById(R.id.textViewTradeDescriptionDetails)
        actionButton = view.findViewById(R.id.buttonTradeActionDetails)

        // Fetch data
        viewModel.fetchTrade(args.tradeId)
        observeTradeState()
    }

    private fun observeTradeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tradeState.collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { trade ->
                            bindTradeData(trade)
                        }
                    }
                    is Resource.Error -> {
                        Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun bindTradeData(trade: Trade) {
        val item = trade.offeredItem
        val proposer = trade.proposer
        val currentUserId = getUserIdFromToken()

        // Use SessionManager
        val currentApiUrl = SessionManager.getServerUrl()
        val baseUrl = currentApiUrl.removeSuffix("api/")

        // Bind Item
        val itemPicUrl = item?.imageUrl?.let { "${baseUrl}api/uploads$it" }
        itemImageView.load(itemPicUrl) {
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_background)
            crossfade(true)
        }
        itemNameTextView.text = item?.name ?: "Unknown Item"
        itemQuantityTextView.text = "Quantity: ${trade.offeredItemQuantity}"
        itemDescriptionTextView.text = item?.description ?: "No description"

        // Bind Proposer
        val profilePicUrl = proposer.profilePictureUrl?.let { "${baseUrl}api/uploads$it" }
        proposerImageView.load(profilePicUrl) {
            placeholder(R.drawable.ic_profile)
            error(R.drawable.ic_profile)
            transformations(CircleCropTransformation())
        }
        proposerNameTextView.text = proposer.username

        proposerReputationTextView.isVisible = false

        // Bind Trade
        tradeDescriptionTextView.text = trade.description ?: "No specific trade details provided."

        // Button Logic
        if (trade.proposerId == currentUserId) {
            // Check if trade is already accepted/active
            if (trade.status == TradeStatus.accepted || trade.status == TradeStatus.completed) {
                actionButton.text = "Go to Chat"
                actionButton.isEnabled = true
                actionButton.setOnClickListener {
                    val action = TradeDetailsFragmentDirections.actionTradeDetailsFragmentToChatFragment(trade.id)
                    findNavController().navigate(action)
                }
            } else {
                // Trade is pending, view applications
                actionButton.text = "View Applications"
                actionButton.isEnabled = true
                actionButton.setOnClickListener {
                    val action = TradeDetailsFragmentDirections
                        .actionTradeDetailsFragmentToTradeApplicationsFragment(trade.id)
                    findNavController().navigate(action)
                }
            }
        } else {
            // Check if user has already applied
            if (trade.myApplication != null) {
                actionButton.text = "Edit Trade Application"
            } else {
                actionButton.text = "Apply for Trade"
            }

            // Disable apply button if trade is no longer pending
            if (trade.status != TradeStatus.pending) {
                actionButton.isEnabled = false
                actionButton.text = "Trade Unavailable"
            } else {
                actionButton.isEnabled = true
                actionButton.alpha = 1.0f
                actionButton.setOnClickListener {
                    val action = TradeDetailsFragmentDirections
                        .actionTradeDetailsFragmentToApplyTradeFragment(
                            tradeId = trade.id,
                            existingApplication = trade.myApplication
                        )
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun getUserIdFromToken(): String? {
        val token = SessionManager.getToken() ?: return null
        try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
            val json = JSONObject(payload)
            return json.optString("sub")
        } catch (_: Exception) { return null }
    }
}
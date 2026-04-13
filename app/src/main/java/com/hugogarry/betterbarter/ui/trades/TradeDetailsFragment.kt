package com.hugogarry.betterbarter.ui.trades

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
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

    // Other Party Views (Dynamic based on who is viewing)
    private lateinit var headerProposerTextView: TextView
    private lateinit var otherPartyImageView: ImageView
    private lateinit var otherPartyNameTextView: TextView
    private lateinit var otherPartyReputationTextView: TextView

    private lateinit var tradeDescriptionTextView: TextView
    private lateinit var actionButton: Button

    // Rating Views
    private lateinit var ratingSection: View
    private lateinit var ratingBar: RatingBar
    private lateinit var ratingCommentEditText: EditText
    private lateinit var submitRatingButton: Button

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

        // Bind main views
        itemImageView = view.findViewById(R.id.imageViewItemDetails)
        itemNameTextView = view.findViewById(R.id.textViewItemNameDetails)
        itemQuantityTextView = view.findViewById(R.id.textViewItemQuantityDetails)
        itemDescriptionTextView = view.findViewById(R.id.textViewItemDescriptionDetails)

        // Bind Other Party Views
        headerProposerTextView = view.findViewById(R.id.headerProposer)
        otherPartyImageView = view.findViewById(R.id.imageViewOtherPartyProfile)
        otherPartyNameTextView = view.findViewById(R.id.textViewOtherPartyName)
        otherPartyReputationTextView = view.findViewById(R.id.textViewOtherPartyReputation)

        tradeDescriptionTextView = view.findViewById(R.id.textViewTradeDescriptionDetails)
        actionButton = view.findViewById(R.id.buttonTradeActionDetails)

        // Bind Rating Views
        ratingSection = view.findViewById(R.id.layoutRatingSection)
        ratingBar = view.findViewById(R.id.ratingBarTrade)
        ratingCommentEditText = view.findViewById(R.id.editTextRatingComment)
        submitRatingButton = view.findViewById(R.id.buttonSubmitRating)

        setupClickListeners()
        observeViewModel()

        // Fetch data
        viewModel.fetchTrade(args.tradeId)
    }

    private fun setupClickListeners() {
        submitRatingButton.setOnClickListener {
            val score = ratingBar.rating.toInt()
            val comment = ratingCommentEditText.text.toString().trim()

            // Send the rating to the ViewModel
            viewModel.rateTrade(args.tradeId, score, comment)
        }
    }

    private fun observeViewModel() {
        // Observe Trade Data
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

        // Observe Rating Submission State
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.ratingState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                        submitRatingButton.isEnabled = false
                        submitRatingButton.text = "Submitting..."
                    }
                    is Resource.Success -> {
                        submitRatingButton.isEnabled = true
                        submitRatingButton.text = "Submit Review"

                        // Hide the rating section immediately on success
                        ratingSection.isVisible = false

                        Toast.makeText(requireContext(), "Rating submitted successfully!", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Error -> {
                        submitRatingButton.isEnabled = true
                        submitRatingButton.text = "Submit Review"
                        Toast.makeText(requireContext(), state.message ?: "Failed to submit rating", Toast.LENGTH_LONG).show()
                    }
                    is Resource.Idle -> {}
                }
            }
        }
    }

    private fun bindTradeData(trade: Trade) {
        val item = trade.offeredItem
        val currentUserId = getUserIdFromToken()

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

        // Determine which user to display in the "Other Party" section
        val partyToShow = if (trade.proposerId == currentUserId && trade.recipient != null) {
            headerProposerTextView.text = "Trade Recipient"
            trade.recipient
        } else {
            headerProposerTextView.text = "Trade Proposer"
            trade.proposer
        }

        // Bind Other Party
        val profilePicUrl = partyToShow.profilePictureUrl?.let { "${baseUrl}api/uploads$it" }
        otherPartyImageView.load(profilePicUrl) {
            placeholder(R.drawable.ic_profile)
            error(R.drawable.ic_profile)
            transformations(CircleCropTransformation())
        }
        otherPartyNameTextView.text = partyToShow.username

        otherPartyReputationTextView.isVisible = true
        val score = partyToShow.reputationScore ?: 0.0
        // Use the badge format compatible with the modernised UI
        otherPartyReputationTextView.text = "%.1f ★".format(score)

        // Bind Trade
        tradeDescriptionTextView.text = trade.description ?: "No specific trade details provided."

        // --- RATING SECTION LOGIC ---
        val isProposer = trade.proposerId == currentUserId
        val hasRated = if (isProposer) trade.isRatedByProposer else trade.isRatedByRecipient

        ratingSection.isVisible = (trade.status == TradeStatus.completed && !hasRated)

        // Button Logic
        if (trade.proposerId == currentUserId) {
            // Check if trade is already accepted/active/completed
            if (trade.status == TradeStatus.accepted || trade.status == TradeStatus.completed) {
                actionButton.text = "Go to Chat"
                actionButton.isEnabled = true
                actionButton.isVisible = true
                actionButton.setOnClickListener {
                    val action = TradeDetailsFragmentDirections.actionTradeDetailsFragmentToChatFragment(trade.id)
                    findNavController().navigate(action)
                }
            } else {
                // Trade is pending, view applications
                actionButton.text = "View Applications"
                actionButton.isEnabled = true
                actionButton.isVisible = true
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
                actionButton.isVisible = false
                actionButton.text = "Trade Unavailable"
            } else {
                actionButton.isEnabled = true
                actionButton.isVisible = true
                actionButton.alpha = 1.0f
                actionButton.setOnClickListener {
                    // Check membership status from the Trade model
                    if (trade.isMember) {
                        val action = TradeDetailsFragmentDirections
                            .actionTradeDetailsFragmentToApplyTradeFragment(
                                tradeId = trade.id,
                                existingApplication = trade.myApplication
                            )
                        findNavController().navigate(action)
                    } else {
                        // User is not a member of the circle this trade belongs to
                        Toast.makeText(context, "Join this circle to propose trades!", Toast.LENGTH_SHORT).show()
                    }
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
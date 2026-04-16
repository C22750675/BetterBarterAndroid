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
import com.hugogarry.betterbarter.data.model.Circle
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

    // Circle Information Views
    private lateinit var circleNameTextView: TextView
    private lateinit var circleIconImageView: ImageView
    private lateinit var cardCircleContext: View

    // Other Party Views
    private lateinit var headerProposerTextView: TextView
    private lateinit var otherPartyImageView: ImageView
    private lateinit var otherPartyNameTextView: TextView
    private lateinit var otherPartyReputationTextView: TextView

    // Trade Context
    private lateinit var tradeDetailsCard: View
    private lateinit var tradeDescriptionTextView: TextView
    private lateinit var actionButton: Button
    private lateinit var raiseDisputeButton: Button

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

        // Bind Circle Views
        circleNameTextView = view.findViewById(R.id.textViewCircleNameDetails)
        circleIconImageView = view.findViewById(R.id.imageViewCircleIconDetails)
        cardCircleContext = view.findViewById(R.id.cardCircleContext)

        // Bind Other Party Views
        headerProposerTextView = view.findViewById(R.id.headerProposer)
        otherPartyImageView = view.findViewById(R.id.imageViewOtherPartyProfile)
        otherPartyNameTextView = view.findViewById(R.id.textViewOtherPartyName)
        otherPartyReputationTextView = view.findViewById(R.id.textViewOtherPartyReputation)

        // Bind Context Views
        tradeDetailsCard = view.findViewById(R.id.cardTradeDetails)
        tradeDescriptionTextView = view.findViewById(R.id.textViewTradeDescriptionDetails)
        actionButton = view.findViewById(R.id.buttonTradeActionDetails)
        raiseDisputeButton = view.findViewById(R.id.buttonRaiseDispute) // Initialize it

        // Bind Rating Views
        ratingSection = view.findViewById(R.id.layoutRatingSection)
        ratingBar = view.findViewById(R.id.ratingBarTrade)
        ratingCommentEditText = view.findViewById(R.id.editTextRatingComment)
        submitRatingButton = view.findViewById(R.id.buttonSubmitRating)

        setupClickListeners()
        observeViewModel()

        viewModel.fetchTradeDetails(args.tradeId)
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
            viewModel.uiState.collectLatest { state ->
                if (state.error != null) {
                    Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
                }

                state.trade?.let { trade ->
                    // Pass both the trade and the populated circle object to the binder
                    bindTradeData(trade, state.circle)
                }
            }
        }

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
                    else -> {}
                }
            }
        }
    }

    private fun bindTradeData(trade: Trade, circle: Circle?) {
        val currentUserId = getUserIdFromToken()
        val currentApiUrl = SessionManager.getServerUrl()
        val baseUrl = currentApiUrl.removeSuffix("api/")

        val item = trade.offeredItem
        val itemPicUrl = item?.imageUrl?.let { "${baseUrl}api/imageUploads$it" }
        itemImageView.load(itemPicUrl) {
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_background)
            crossfade(true)
        }
        itemNameTextView.text = item?.name ?: "Unknown Item"
        itemQuantityTextView.text = "Quantity: ${trade.offeredItemQuantity}"
        itemDescriptionTextView.text = item?.description ?: "No description"

        // Display the Circle Name and Load the Circle Icon correctly
        circleNameTextView.text = circle?.name ?: "Unknown Circle"

        val circleIconPath = circle?.imageUrl?.removePrefix("/")
        val circleIconUrl = circleIconPath?.let { "${baseUrl}api/imageUploads/$it" }
        circleIconImageView.load(circleIconUrl) {
            placeholder(R.drawable.ic_circles)
            error(R.drawable.ic_circles)
            transformations(CircleCropTransformation())
        }

        val partyToShow = if (trade.proposerId == currentUserId && trade.recipient != null) {
            headerProposerTextView.text = "Trade Recipient"
            trade.recipient
        } else {
            headerProposerTextView.text = "Trade Proposer"
            trade.proposer
        }

        // Bind Other Party
        val profilePicUrl = partyToShow?.profilePictureUrl?.let { "${baseUrl}api/imageUploads$it" }
        otherPartyImageView.load(profilePicUrl) {
            placeholder(R.drawable.ic_profile)
            error(R.drawable.ic_profile)
            transformations(CircleCropTransformation())
        }
        otherPartyNameTextView.text = partyToShow?.username ?: "Unknown User"
        otherPartyReputationTextView.text = "%.1f ★".format(partyToShow?.reputationScore ?: 0.0)

        // Logic to hide the description box if there is no description
        if (trade.description.isNullOrBlank()) {
            tradeDetailsCard.isVisible = false
        } else {
            tradeDetailsCard.isVisible = true
            tradeDescriptionTextView.text = trade.description
        }

        val isProposer = trade.proposerId == currentUserId
        val isRecipient = trade.recipient?.id == currentUserId
        val hasRated = if (isProposer) trade.isRatedByProposer else trade.isRatedByRecipient
        ratingSection.isVisible = (trade.status == TradeStatus.completed && !hasRated)

        // Action Button Logic
        actionButton.isVisible = true

        if (isProposer) {
            if (trade.status == TradeStatus.accepted || trade.status == TradeStatus.completed) {
                actionButton.text = "Go to Chat"
                actionButton.setOnClickListener {
                    val action = TradeDetailsFragmentDirections.actionTradeDetailsFragmentToChatFragment(trade.id)
                    findNavController().navigate(action)
                }
            } else {
                actionButton.text = "View Applications"
                actionButton.setOnClickListener {
                    val action = TradeDetailsFragmentDirections.actionTradeDetailsFragmentToTradeApplicationsFragment(trade.id)
                    findNavController().navigate(action)
                }
            }
        } else {
            if (trade.status == TradeStatus.accepted || trade.status == TradeStatus.completed) {
                if (isRecipient) {
                    actionButton.isVisible = true
                    actionButton.text = "Go to Chat"
                    actionButton.setOnClickListener {
                        val action = TradeDetailsFragmentDirections.actionTradeDetailsFragmentToChatFragment(trade.id)
                        findNavController().navigate(action)
                    }
                } else {
                    actionButton.isVisible = false
                }
            } else {
                actionButton.text = if (trade.myApplication != null) "Edit Trade Application" else "Apply for Trade"
                if (trade.status != TradeStatus.pending) {
                    actionButton.isVisible = false
                } else {
                    actionButton.isVisible = true
                    actionButton.setOnClickListener {
                        if (viewModel.uiState.value.isMember) {
                            val action = TradeDetailsFragmentDirections
                                .actionTradeDetailsFragmentToApplyTradeFragment(
                                    tradeId = trade.id,
                                    existingApplication = trade.myApplication
                                )
                            findNavController().navigate(action)
                        } else {
                            Toast.makeText(context, "Join this circle to propose trades!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        // Raise Dispute Button Logic
        // Only show if trade is in progress or completed AND the current user is part of the transaction
        if ((trade.status == TradeStatus.accepted || trade.status == TradeStatus.completed) && (isProposer || isRecipient)) {
            raiseDisputeButton.isVisible = true
            raiseDisputeButton.setOnClickListener {
                val action = TradeDetailsFragmentDirections.actionTradeDetailsFragmentToRaiseDisputeFragment(trade.id)
                findNavController().navigate(action)
            }
        } else {
            raiseDisputeButton.isVisible = false
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
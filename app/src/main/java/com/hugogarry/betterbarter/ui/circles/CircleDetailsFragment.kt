package com.hugogarry.betterbarter.ui.circles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.data.model.Trade
import com.hugogarry.betterbarter.util.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject

class CircleDetailsFragment : Fragment() {

    private val viewModel: CircleDetailsViewModel by viewModels()

    private lateinit var toolbar: Toolbar
    private lateinit var collapsingToolbar: CollapsingToolbarLayout
    private lateinit var fabAddTrade: FloatingActionButton
    private lateinit var descriptionText: TextView
    private lateinit var adminText: TextView
    private lateinit var headerImageView: ImageView
    private lateinit var btnAdminDisputes: Button

    private lateinit var availableTradesAdapter: AvailableTradesAdapter
    private lateinit var recyclerViewActiveTrades: RecyclerView
    private lateinit var progressBarActiveTrades: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_circle_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        descriptionText = view.findViewById(R.id.textViewCircleDescription)
        adminText = view.findViewById(R.id.textViewAdmins)
        toolbar = view.findViewById(R.id.toolbar)
        collapsingToolbar = view.findViewById(R.id.collapsingToolbar)
        fabAddTrade = view.findViewById(R.id.fabAddTrade)
        headerImageView = view.findViewById(R.id.imageViewCircleHeader)
        btnAdminDisputes = view.findViewById(R.id.btnAdminDisputes)

        recyclerViewActiveTrades = view.findViewById(R.id.recyclerViewAvailableTrades)
        progressBarActiveTrades = view.findViewById(R.id.progressBarAvailableTrades)

        NavigationUI.setupWithNavController(toolbar, findNavController())

        setupRecyclerView()

        fabAddTrade.setOnClickListener {
            val action = CircleDetailsFragmentDirections
                .actionCircleDetailsFragmentToCreateTradeFragment(viewModel.circleId)
            findNavController().navigate(action)
        }

        btnAdminDisputes.setOnClickListener {
            val action = CircleDetailsFragmentDirections
                .actionCircleDetailsFragmentToAdminDisputesFragment(viewModel.circleId)
            findNavController().navigate(action)
        }

        observeUiState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchScreenData()
    }

    private fun setupRecyclerView() {
        val currentUserId = getUserIdFromToken() ?: ""
        availableTradesAdapter = AvailableTradesAdapter(currentUserId)

        availableTradesAdapter.onProposeClick = { trade: Trade ->
            val isMember = viewModel.uiState.value.isMember

            if (trade.proposerId == currentUserId) {
                // Owner: Navigate to CreateTradeFragment and pass the tradeId for editing
                val action = CircleDetailsFragmentDirections
                    .actionCircleDetailsFragmentToCreateTradeFragment(
                        circleId = trade.circleId,
                        tradeId = trade.id
                    )
                findNavController().navigate(action)
            } else if (isMember) {
                // Member: Navigate to ApplyTradeFragment
                val action = CircleDetailsFragmentDirections
                    .actionCircleDetailsFragmentToApplyTradeFragment(
                        tradeId = trade.id,
                        existingApplication = trade.myApplication
                    )
                findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "Join this circle to propose trades!", Toast.LENGTH_SHORT).show()
            }
        }

        availableTradesAdapter.onDeleteClick = { trade ->
            showDeleteConfirmation(trade)
        }

        availableTradesAdapter.onItemClick = { trade: Trade ->
            val action = CircleDetailsFragmentDirections
                .actionCircleDetailsFragmentToTradeDetailsFragment(trade.id)
            findNavController().navigate(action)
        }

        recyclerViewActiveTrades.apply {
            adapter = availableTradesAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun showDeleteConfirmation(trade: Trade) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Trade Proposal?")
            .setMessage("Are you sure you want to delete your proposal for '${trade.offeredItem?.name}'? This cannot be undone.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteTrade(trade.id)
            }
            .show()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                progressBarActiveTrades.isVisible = state.isLoading

                if (state.error != null) {
                    Toast.makeText(context, state.error, Toast.LENGTH_LONG).show()
                }

                state.circle?.let { circle ->
                    // Set the title on the CollapsingToolbar to handle large expanded text and small collapsed text
                    collapsingToolbar.title = circle.name

                    descriptionText.text = circle.description
                    adminText.text = "Admins: ${circle.admins?.joinToString { it.username }}"

                    // Load the header image
                    val currentApiUrl = SessionManager.getServerUrl()
                    val baseUrl = currentApiUrl.removeSuffix("api/")
                    val path = circle.imageUrl?.removePrefix("/")
                    val fullImageUrl = path?.let { "${baseUrl}api/uploads/$it" }

                    headerImageView.load(fullImageUrl) {
                        crossfade(true)
                        placeholder(R.drawable.ic_circles)
                        error(R.drawable.ic_circles)
                    }

                    // Toggle Visibility based on Membership / Admin status
                    val currentUserId = getUserIdFromToken()
                    val isAdmin = circle.admins?.any { it.id == currentUserId } == true

                    btnAdminDisputes.isVisible = isAdmin
                    fabAddTrade.isVisible = !state.isLoading && state.isMember
                }

                availableTradesAdapter.submitList(state.availableTrades)
            }
        }
    }

    // Helper to extract ID from JWT
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
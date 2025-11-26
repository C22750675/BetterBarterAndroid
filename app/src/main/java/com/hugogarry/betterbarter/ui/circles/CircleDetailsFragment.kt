package com.hugogarry.betterbarter.ui.circles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private lateinit var fabAddTrade: FloatingActionButton
    private lateinit var descriptionText: TextView
    private lateinit var adminText: TextView

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
        fabAddTrade = view.findViewById(R.id.fabAddTrade)

        recyclerViewActiveTrades = view.findViewById(R.id.recyclerViewAvailableTrades)
        progressBarActiveTrades = view.findViewById(R.id.progressBarAvailableTrades)

        NavigationUI.setupWithNavController(toolbar, findNavController())

        setupRecyclerView()

        fabAddTrade.setOnClickListener {
            val action = CircleDetailsFragmentDirections
                .actionCircleDetailsFragmentToCreateTradeFragment(viewModel.circleId)
            findNavController().navigate(action)
        }

        observeUiState()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this screen (e.g., after applying for a trade)
        viewModel.fetchScreenData()
    }

    private fun setupRecyclerView() {
        // Get User ID from token
        val currentUserId = getUserIdFromToken() ?: ""

        availableTradesAdapter = AvailableTradesAdapter(currentUserId)

        availableTradesAdapter.onProposeClick = { trade: Trade ->
            // Navigate to Apply Trade Fragment with existing app if present
            val action = CircleDetailsFragmentDirections
                .actionCircleDetailsFragmentToApplyTradeFragment(
                    tradeId = trade.id,
                    existingApplication = trade.myApplication
                )
            findNavController().navigate(action)
        }

        // Handle Item Click to navigate to Details
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

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                progressBarActiveTrades.isVisible = state.isLoading

                if (state.error != null) {
                    Toast.makeText(context, state.error, Toast.LENGTH_LONG).show()
                }

                state.circle?.let { circle ->
                    toolbar.title = circle.name
                    descriptionText.text = circle.description
                    adminText.text = "Admins: ${circle.admins?.joinToString { it.username }}"
                }

                availableTradesAdapter.submitList(state.availableTrades)

                fabAddTrade.isVisible = !state.isLoading
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
        } catch (e: Exception) { return null }
    }
}
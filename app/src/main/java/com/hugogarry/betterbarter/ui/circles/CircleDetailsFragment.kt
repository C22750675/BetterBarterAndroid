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
import com.hugogarry.betterbarter.data.model.Trade // <-- Make sure this is imported
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

class CircleDetailsFragment : Fragment() {

    private val viewModel: CircleDetailsViewModel by viewModels()

    private lateinit var toolbar: Toolbar
    private lateinit var fabAddTrade: FloatingActionButton
    private lateinit var descriptionText: TextView
    private lateinit var adminText: TextView

    private lateinit var activeTradesAdapter: ActiveTradesAdapter
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

        // Find all views
        descriptionText = view.findViewById(R.id.textViewCircleDescription)
        adminText = view.findViewById(R.id.textViewAdmins)
        toolbar = view.findViewById(R.id.toolbar)
        fabAddTrade = view.findViewById(R.id.fabAddTrade)

        recyclerViewActiveTrades = view.findViewById(R.id.recyclerViewActiveTrades)
        progressBarActiveTrades = view.findViewById(R.id.progressBarActiveTrades)

        // Setup toolbar
        NavigationUI.setupWithNavController(toolbar, findNavController())

        setupRecyclerView()

        fabAddTrade.setOnClickListener {
            // Create the action, passing the circleId
            val action = CircleDetailsFragmentDirections
                .actionCircleDetailsFragmentToCreateTradeFragment(viewModel.circleId)
            findNavController().navigate(action)
        }

        observeUiState()
    }

    private fun setupRecyclerView() {
        activeTradesAdapter = ActiveTradesAdapter()
        // --- THIS IS THE FIX ---
        activeTradesAdapter.onProposeClick = { trade: Trade -> // <-- Explicitly set type to Trade
            // TODO: Navigate to trade proposal screen
            Toast.makeText(context, "Propose on ${trade.offeredItem?.name}", Toast.LENGTH_SHORT).show()
        }
        // --- END OF FIX ---

        recyclerViewActiveTrades.apply {
            adapter = activeTradesAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                // Show loading on the *list* progress bar
                progressBarActiveTrades.isVisible = state.isLoading

                // Handle Error
                if (state.error != null) {
                    Toast.makeText(context, state.error, Toast.LENGTH_LONG).show()
                    // You might want a dedicated error text view
                }

                // Handle Success data
                state.circle?.let { circle ->
                    toolbar.title = circle.name
                    descriptionText.text = circle.description
                    adminText.text = "Admins: ${circle.admins?.joinToString { it.username }}"
                }

                // Submit the list of Trades
                activeTradesAdapter.submitList(state.activeTrades)

                // The main logic for the FAB
                fabAddTrade.isVisible = state.isUserAdmin
            }
        }
    }
}
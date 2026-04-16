package com.hugogarry.betterbarter.ui.trades

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.data.model.Trade
import com.hugogarry.betterbarter.data.model.TradeStatus
import com.hugogarry.betterbarter.util.Resource
import com.hugogarry.betterbarter.util.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject

class TradesFragment : Fragment() {

    private val viewModel: TradesViewModel by viewModels()
    private lateinit var adapter: MyTradesAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trades, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = getUserIdFromToken() ?: ""

        adapter = MyTradesAdapter(userId) { trade, action ->
            when (action) {
                MyTradesAdapter.ActionType.ACCEPT -> viewModel.updateStatus(trade, TradeStatus.accepted)
                MyTradesAdapter.ActionType.CANCEL -> viewModel.updateStatus(trade, TradeStatus.cancelled)
                MyTradesAdapter.ActionType.COMPLETE -> viewModel.updateStatus(trade, TradeStatus.completed)
                MyTradesAdapter.ActionType.EDIT_PROPOSAL -> {
                    // Navigate to CreateTradeFragment with tradeId to trigger edit mode
                    val navAction = TradesFragmentDirections
                        .actionTradesFragmentToCreateTradeFragment(
                            circleId = trade.circleId,
                            tradeId = trade.id
                        )
                    findNavController().navigate(navAction)
                }
                MyTradesAdapter.ActionType.DELETE -> {
                    showDeleteConfirmation(trade)
                }
            }
        }

        // Handle Item Click to navigate to TradeDetailsFragment
        adapter.onItemClick = { trade ->
            val action = TradesFragmentDirections
                .actionTradesFragmentToTradeDetailsFragment(trade.id)
            findNavController().navigate(action)
        }

        recyclerView = view.findViewById(R.id.recyclerViewTrades)
        progressBar = view.findViewById(R.id.progressBarTrades)
        emptyStateLayout = view.findViewById(R.id.emptyState)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.fetchMyTrades()
        observeState()
    }

    private fun showDeleteConfirmation(trade: Trade) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Trade Proposal?")
            .setMessage("Are you sure you want to delete your proposal for '${trade.offeredItem?.name}'? This action cannot be undone.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteTrade(trade.id)
            }
            .show()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.trades.collectLatest { resource ->
                progressBar.isVisible = resource is Resource.Loading

                when (resource) {
                    is Resource.Success -> {
                        val isEmpty = resource.data.isNullOrEmpty()
                        emptyStateLayout.isVisible = isEmpty
                        recyclerView.isVisible = !isEmpty
                        adapter.submitList(resource.data ?: emptyList())
                    }
                    is Resource.Error -> {
                        emptyStateLayout.isVisible = true
                        recyclerView.isVisible = false
                        Toast.makeText(context, resource.message ?: "Failed to load trades", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Loading, is Resource.Idle -> {
                        emptyStateLayout.isVisible = false
                        recyclerView.isVisible = false
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.actionStatus.collectLatest { resource ->
                if (resource is Resource.Success) {
                    Toast.makeText(context, resource.data, Toast.LENGTH_SHORT).show()
                    viewModel.clearStatus()
                } else if (resource is Resource.Error) {
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                    viewModel.clearStatus()
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
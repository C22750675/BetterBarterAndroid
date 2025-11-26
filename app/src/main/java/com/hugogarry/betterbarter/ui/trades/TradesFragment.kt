package com.hugogarry.betterbarter.ui.trades

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.data.model.Trade
import com.hugogarry.betterbarter.data.model.TradeStatus
import com.hugogarry.betterbarter.util.Resource
import com.hugogarry.betterbarter.util.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Base64

class TradesFragment : Fragment() {

    private val viewModel: TradesViewModel by viewModels()
    private lateinit var adapter: MyTradesAdapter
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trades, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Decode JWT to get User ID (Simple approach for MVP)
        val userId = getUserIdFromToken() ?: ""

        adapter = MyTradesAdapter(userId) { trade, action ->
            when (action) {
                MyTradesAdapter.ActionType.ACCEPT -> viewModel.updateStatus(trade, TradeStatus.accepted)
                MyTradesAdapter.ActionType.REJECT -> viewModel.updateStatus(trade, TradeStatus.rejected)
                MyTradesAdapter.ActionType.COMPLETE -> viewModel.updateStatus(trade, TradeStatus.completed)
                MyTradesAdapter.ActionType.RATE -> showRatingDialog(trade)
            }
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewTrades) // Need to add ID to layout
        // NOTE: You need to add android:id="@+id/recyclerViewTrades" to your fragment_trades.xml
        // If it's not there, add it now. Assuming you have a basic RecyclerView there.

        // Wait, your uploaded fragment_trades.xml only had a TextView.
        // I will assume you can swap that TextView for a RecyclerView in your IDE.
        // Or I can provide the layout file update below.

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.fetchMyTrades()
        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.trades.collectLatest { resource ->
                if (resource is Resource.Success) {
                    adapter.submitList(resource.data)
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

    private fun showRatingDialog(trade: Trade) {
        val input = EditText(context)
        input.hint = "Comment (Optional)"

        AlertDialog.Builder(context)
            .setTitle("Rate this trade (1-5)")
            .setView(input)
            .setPositiveButton("5 Stars") { _, _ -> viewModel.submitRating(trade, 5, input.text.toString()) }
            .setNegativeButton("Cancel", null)
            .show()
    }

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
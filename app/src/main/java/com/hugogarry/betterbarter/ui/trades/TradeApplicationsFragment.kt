package com.hugogarry.betterbarter.ui.trades

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
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TradeApplicationsFragment : Fragment() {

    private val viewModel: TradeApplicationsViewModel by viewModels()
    private val args: TradeApplicationsFragmentArgs by navArgs()
    private lateinit var adapter: TradeApplicationsAdapter

    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var errorView: TextView
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trade_applications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbarApplications)
        NavigationUI.setupWithNavController(toolbar, findNavController())

        progressBar = view.findViewById(R.id.progressBarApplications)
        emptyView = view.findViewById(R.id.textViewNoApplications)
        errorView = view.findViewById(R.id.textViewError)
        recyclerView = view.findViewById(R.id.recyclerViewApplications)

        adapter = TradeApplicationsAdapter(
            onAccept = { app ->
                viewModel.acceptApplication(app)
            },
            onDecline = { app ->
                viewModel.declineApplication(app)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        viewModel.fetchApplications(args.tradeId)
        observeState()
        observeActionState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.applications.collectLatest { resource ->
                progressBar.isVisible = resource is Resource.Loading
                errorView.isVisible = resource is Resource.Error
                emptyView.isVisible = resource is Resource.Success && resource.data.isNullOrEmpty()
                recyclerView.isVisible = resource is Resource.Success && !resource.data.isNullOrEmpty()

                if (resource is Resource.Success) {
                    adapter.submitList(resource.data)
                } else if (resource is Resource.Error) {
                    errorView.text = resource.message
                }
            }
        }
    }

    private fun observeActionState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.actionState.collectLatest { resource ->
                if (resource is Resource.Success) {
                    // Handle specific result types differently
                    when (val result = resource.data) {
                        is TradeActionResult.Accepted -> {
                            Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                            // Navigate to Chat
                            val action = TradeApplicationsFragmentDirections
                                .actionTradeApplicationsFragmentToChatFragment(result.tradeId)
                            findNavController().navigate(action)
                        }
                        is TradeActionResult.Declined -> {
                            Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                        }
                        null -> { /* Should not happen if data is non-nullable in Success */ }
                    }

                    viewModel.clearActionState()
                } else if (resource is Resource.Error) {
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                    viewModel.clearActionState()
                }
            }
        }
    }
}
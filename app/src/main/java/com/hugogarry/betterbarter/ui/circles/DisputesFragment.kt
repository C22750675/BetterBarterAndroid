package com.hugogarry.betterbarter.ui.circles

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
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DisputesFragment : Fragment() {

    private val viewModel: DisputesViewModel by viewModels()
    private lateinit var adapter: DisputesAdapter
    private val args: DisputesFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_disputes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbarDisputes)
        // Automatically adds the back button and handles the click using Jetpack Navigation
        NavigationUI.setupWithNavController(toolbar, findNavController())

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewDisputes)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val emptyStateLayout = view.findViewById<LinearLayout>(R.id.emptyState)

        adapter = DisputesAdapter { dispute ->
            val action = DisputesFragmentDirections.actionDisputesFragmentToDisputeDetailsFragment(dispute.id)
            findNavController().navigate(action)
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        viewModel.fetchDisputes(args.circleId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.disputes.collectLatest { resource ->
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
                        Toast.makeText(context, resource.message ?: "Failed to load disputes", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Loading, is Resource.Idle -> {
                        emptyStateLayout.isVisible = false
                        recyclerView.isVisible = false
                    }
                }
            }
        }
    }
}
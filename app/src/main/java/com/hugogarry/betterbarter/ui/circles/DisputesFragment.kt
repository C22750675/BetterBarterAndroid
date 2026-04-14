package com.hugogarry.betterbarter.ui.circles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
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

    // Note: If you eventually rename the fragment in nav_graph.xml,
    // you may also need to update AdminDisputesFragmentArgs to DisputesFragmentArgs
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
        val emptyText = view.findViewById<TextView>(R.id.textEmpty)

        adapter = DisputesAdapter { dispute ->
            // Update this NavDirection too if the ID changes in nav_graph.xml
            val action = DisputesFragmentDirections.actionAdminDisputesFragmentToAdminDisputeDetailsFragment(dispute.id)
            findNavController().navigate(action)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        viewModel.fetchDisputes(args.circleId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.disputes.collectLatest { resource ->
                progressBar.isVisible = resource is Resource.Loading
                emptyText.isVisible = resource is Resource.Success && resource.data.isNullOrEmpty()
                recyclerView.isVisible = resource is Resource.Success && !resource.data.isNullOrEmpty()

                if (resource is Resource.Success) {
                    adapter.submitList(resource.data)
                }
            }
        }
    }
}
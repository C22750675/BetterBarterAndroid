package com.hugogarry.betterbarter.ui.circles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.ui.profile.ProfileItemsAdapter
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

class CircleDetailsFragment : Fragment() {

    private val viewModel: CircleDetailsViewModel by viewModels()

    private lateinit var itemsAdapter: ProfileItemsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var descriptionText: TextView
    private lateinit var adminText: TextView
    private lateinit var toolbar: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_circle_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find all views
        recyclerView = view.findViewById(R.id.recyclerViewCircleItems)
        progressBar = view.findViewById(R.id.progressBarItems)
        descriptionText = view.findViewById(R.id.textViewCircleDescription)
        adminText = view.findViewById(R.id.textViewAdmins)
        toolbar = view.findViewById(R.id.toolbar)

        // Setup toolbar
        NavigationUI.setupWithNavController(toolbar, findNavController())

        setupRecyclerView()
        observeCircleDetails()
        observeCircleItems()
    }

    private fun setupRecyclerView() {
        itemsAdapter = ProfileItemsAdapter()
        // --- TODO for Step 5 ---
        // itemsAdapter.onItemClick = { item ->
        //    val action = CircleDetailFragmentDirections.actionCircleDetailFragmentToTradeProposalFragment(item.id)
        //    findNavController().navigate(action)
        // }
        // --- End TODO ---

        recyclerView.apply {
            adapter = itemsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeCircleDetails() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.circleDetails.collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val circle = resource.data!!
                        toolbar.title = circle.name
                        descriptionText.text = circle.description
                        // Format the admin list
                        adminText.text = "Admins: ${circle.admins?.joinToString { it.username }}"
                    }
                    is Resource.Error -> {
                        toolbar.title = "Error"
                        descriptionText.text = resource.message
                    }
                    is Resource.Loading -> {
                        toolbar.title = "Loading..."
                    }
                    else -> {}
                }
            }
        }
    }

    private fun observeCircleItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.circleItems.collectLatest { resource ->
                progressBar.isVisible = resource is Resource.Loading
                recyclerView.isVisible = resource is Resource.Success

                when (resource) {
                    is Resource.Success -> {
                        itemsAdapter.submitList(resource.data)
                    }
                    is Resource.Error -> {
                        // You might want a dedicated error TextView for this list
                    }
                    else -> {}
                }
            }
        }
    }
}
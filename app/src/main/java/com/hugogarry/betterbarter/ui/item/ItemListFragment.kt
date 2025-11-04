package com.hugogarry.betterbarter.ui.item

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
import androidx.recyclerview.widget.RecyclerView
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ItemListFragment : Fragment() {

    // Use the KTX delegate to get a reference to the ViewModel
    private val viewModel: ItemListViewModel by viewModels()

    // Declare your view references
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize your views
        recyclerView = view.findViewById(R.id.recyclerViewItems)
        progressBar = view.findViewById(R.id.progressBar)
        errorTextView = view.findViewById(R.id.textViewError)

        // Start observing the state from the ViewModel
        observeItems()

        // Trigger the data fetch. You would pass a real circle ID here.
        viewModel.fetchItemsForCircle("some-dummy-circle-id")
    }

    private fun observeItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.items.collectLatest { resource ->
                // Handle visibility based on the state type directly
                progressBar.isVisible = resource is Resource.Loading
                recyclerView.isVisible = resource is Resource.Success
                errorTextView.isVisible = resource is Resource.Error

                // Handle specific data updates within the when block
                when (resource) {
                    is Resource.Success -> {
                        // The list is already visible, just submit the data
                        // to your adapter here.
                    }

                    is Resource.Error -> {
                        // The error text view is already visible, just set the text.
                        errorTextView.text = resource.message
                    }

                    is Resource.Loading, is Resource.Idle -> {
                        // No extra action needed, visibility is already handled.
                    }
                }
            }
        }
    }
}
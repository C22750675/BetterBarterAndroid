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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CirclesFragment : Fragment() {

    private val viewModel: CirclesViewModel by viewModels()
    private lateinit var circlesAdapter: CirclesAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_circles, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewCircles)
        progressBar = view.findViewById(R.id.progressBarCircles)
        errorTextView = view.findViewById(R.id.textViewErrorCircles)

        val fab = view.findViewById<FloatingActionButton>(R.id.fabAddCircle)
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_circlesFragment_to_createCircleFragment)
        }

        setupRecyclerView()
        observeCirclesState()
    }

    private fun setupRecyclerView() {
        circlesAdapter = CirclesAdapter()
        recyclerView.apply {
            adapter = circlesAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeCirclesState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.circlesState.collectLatest { resource ->
                progressBar.isVisible = resource is Resource.Loading
                recyclerView.isVisible = resource is Resource.Success
                errorTextView.isVisible = resource is Resource.Error

                when (resource) {
                    is Resource.Success -> {
                        circlesAdapter.submitList(resource.data)
                        if (resource.data.isNullOrEmpty()) {
                            errorTextView.isVisible = true
                            errorTextView.text = "You haven't joined any circles yet."
                        }
                    }
                    is Resource.Error -> {
                        errorTextView.text = resource.message
                    }
                    is Resource.Loading, is Resource.Idle -> {
                        // Handled by visibility bindings
                    }
                }
            }
        }
    }
}
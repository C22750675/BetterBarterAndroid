package com.hugogarry.betterbarter.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hugogarry.betterbarter.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var itemsAdapter: ProfileItemsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameTextView = view.findViewById<TextView>(R.id.textViewUsername)
        val fabAddItem = view.findViewById<FloatingActionButton>(R.id.fabAddItem)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewProfileItems)

        // Setup RecyclerView
        itemsAdapter = ProfileItemsAdapter()
        recyclerView.adapter = itemsAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Setup FAB click listener
        fabAddItem.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_addItemFragment)
        }

        // Observe UI state from the ViewModel
        observeUiState(usernameTextView)
    }

    private fun observeUiState(usernameTextView: TextView) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                // TODO: Handle loading state with a ProgressBar
                // progressBar.isVisible = state.isLoading

                if (state.user != null) {
                    usernameTextView.text = state.user.username
                    // TODO: Load profile picture
                }

                itemsAdapter.submitList(state.items)

                if (state.error != null) {
                    Toast.makeText(context, "Error: ${state.error}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
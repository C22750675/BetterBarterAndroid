package com.hugogarry.betterbarter.ui.chat

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
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyChatsFragment : Fragment() {

    private val viewModel: MyChatsViewModel by viewModels()
    private lateinit var adapter: MyChatsAdapter

    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var errorView: TextView
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_chats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBarChats)
        emptyView = view.findViewById(R.id.textViewNoChats)
        errorView = view.findViewById(R.id.textViewError)
        recyclerView = view.findViewById(R.id.recyclerViewChats)

        adapter = MyChatsAdapter { chat ->
            // Navigate to Chat Fragment
            val action = MyChatsFragmentDirections.actionMyChatsFragmentToChatFragment(chat.id)
            findNavController().navigate(action)
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        viewModel.fetchChats()
        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.chats.collectLatest { resource ->
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
}
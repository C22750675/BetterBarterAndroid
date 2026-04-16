package com.hugogarry.betterbarter.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.util.Resource
import com.hugogarry.betterbarter.util.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChatFragment : Fragment() {

    private val viewModel: ChatViewModel by viewModels()
    private val args: ChatFragmentArgs by navArgs()
    private lateinit var adapter: ChatAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var tradeBannerLayout: View
    private lateinit var tradeItemImage: ImageView
    private lateinit var tradeItemName: TextView
    private lateinit var tradeStatus: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbarChat)
        NavigationUI.setupWithNavController(toolbar, findNavController())

        recyclerView = view.findViewById(R.id.recyclerViewMessages)
        messageInput = view.findViewById(R.id.editTextMessage)
        sendButton = view.findViewById(R.id.buttonSend)
        tradeBannerLayout = view.findViewById(R.id.layoutTradeBanner)
        tradeItemImage = view.findViewById(R.id.imageViewTradeItem)
        tradeItemName = view.findViewById(R.id.textViewTradeItemName)
        tradeStatus = view.findViewById(R.id.textViewTradeStatus)

        val currentUserId = getUserIdFromToken() ?: ""
        adapter = ChatAdapter(currentUserId)

        recyclerView.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = adapter

        sendButton.setOnClickListener {
            // Trim the text to remove leading/trailing whitespace
            val text = messageInput.text.toString().trim()

            // Only send if the message is not empty after trimming
            if (text.isNotEmpty()) {
                viewModel.sendMessage(args.tradeId, text)
                messageInput.text.clear()
            }
        }

        // Navigate to trade details when the banner is clicked
        tradeBannerLayout.setOnClickListener {
            val action = ChatFragmentDirections.actionChatFragmentToTradeDetailsFragment(args.tradeId)
            findNavController().navigate(action)
        }

        viewModel.fetchMessages(args.tradeId)
        viewModel.fetchTradeDetails(args.tradeId)

        observeMessages()
        observeTradeDetails(toolbar)
    }

    private fun observeMessages() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.messages.collectLatest { resource ->
                if (resource is Resource.Success) {
                    adapter.submitList(resource.data)
                    // Scroll to bottom on new messages
                    if (!resource.data.isNullOrEmpty()) {
                        recyclerView.smoothScrollToPosition(resource.data.size - 1)
                    }
                } else if (resource is Resource.Error) {
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeTradeDetails(toolbar: Toolbar) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tradeDetails.collectLatest { resource ->
                if (resource is Resource.Success) {
                    val trade = resource.data!!
                    val item = trade.offeredItem
                    val currentUserId = getUserIdFromToken()

                    // Dynamic URL
                    val currentApiUrl = SessionManager.getServerUrl()
                    val baseUrl = currentApiUrl.removeSuffix("api/")

                    // Set Toolbar Title to other user's name
                    val otherUserName = if (trade.proposerId == currentUserId) {
                        trade.recipient?.username
                    } else {
                        trade.proposer?.username
                    }
                    toolbar.title = otherUserName ?: "Chat"

                    // Set Item Details
                    tradeItemName.text = "${item?.name ?: "Unknown Item"} (${trade.offeredItemQuantity})"
                    tradeStatus.text = "Status: ${trade.status.name.uppercase()}"

                    val itemUrl = item?.imageUrl?.let { "${baseUrl}api/imageUploads$it" }
                    tradeItemImage.load(itemUrl) {
                        placeholder(R.drawable.ic_launcher_background)
                        error(R.drawable.ic_launcher_background)
                    }
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
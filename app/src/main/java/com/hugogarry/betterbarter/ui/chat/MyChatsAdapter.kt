package com.hugogarry.betterbarter.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.hugogarry.betterbarter.BuildConfig
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.data.model.Chat

class MyChatsAdapter(
    private val onChatClick: (Chat) -> Unit
) : ListAdapter<Chat, MyChatsAdapter.ChatViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = getItem(position)
        holder.bind(chat, onChatClick)
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ImageView = itemView.findViewById(R.id.imageViewChatProfile)
        private val nameText: TextView = itemView.findViewById(R.id.textViewChatName)
        private val messageText: TextView = itemView.findViewById(R.id.textViewLastMessage)
        private val timeText: TextView = itemView.findViewById(R.id.textViewChatTime)
        private val statusText: TextView = itemView.findViewById(R.id.textViewTradeStatus)

        private val baseUrl = BuildConfig.BASE_URL.removeSuffix("/api/")

        fun bind(chat: Chat, onClick: (Chat) -> Unit) {
            nameText.text = chat.otherUser.username
            messageText.text = chat.lastMessage ?: "Start the conversation!"

            // Simple date formatting (you might want a better utility for "Just now", "Yesterday", etc.)
            timeText.text = chat.lastMessageTimestamp?.let {
                try {
                    // Assuming ISO string from backend, simple substring for time
                    it.substring(11, 16)
                } catch (e: Exception) { "" }
            } ?: ""

            statusText.text = chat.tradeStatus.name

            val profileUrl = chat.otherUser.profilePictureUrl?.let { "$baseUrl/api/uploads$it" }
            profileImage.load(profileUrl) {
                placeholder(R.drawable.ic_profile)
                error(R.drawable.ic_profile)
                transformations(CircleCropTransformation())
            }

            itemView.setOnClickListener { onClick(chat) }
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean = oldItem == newItem
    }
}
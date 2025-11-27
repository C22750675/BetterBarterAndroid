package com.hugogarry.betterbarter.ui.chat

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.data.model.Message

class ChatAdapter(private val currentUserId: String) : ListAdapter<Message, ChatAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message, currentUserId)
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageContainer: LinearLayout = itemView.findViewById(R.id.messageContainer)
        private val messageBody: TextView = itemView.findViewById(R.id.textViewMessageBody)
        private val messageTime: TextView = itemView.findViewById(R.id.textViewMessageTime)

        fun bind(message: Message, currentUserId: String) {
            messageBody.text = message.text

            try {
                // Assuming ISO 8601 format from backend: "2023-10-27T10:00:00.000Z"
                val time = message.timestamp.substring(11, 16)
                messageTime.text = time
            } catch (e: Exception) {
                messageTime.text = ""
            }

            if (message.senderId == currentUserId) {
                // Sent: Align Right, Primary Color Background
                messageContainer.gravity = Gravity.END
                messageBody.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_message_sent)
                messageBody.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))

                val params = messageBody.layoutParams as LinearLayout.LayoutParams
                params.gravity = Gravity.END
                messageBody.layoutParams = params

                val timeParams = messageTime.layoutParams as LinearLayout.LayoutParams
                timeParams.gravity = Gravity.END
                messageTime.layoutParams = timeParams

            } else {
                // Received: Align Left, Grey Background
                messageContainer.gravity = Gravity.START
                messageBody.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_message_received)
                messageBody.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.black))

                val params = messageBody.layoutParams as LinearLayout.LayoutParams
                params.gravity = Gravity.START
                messageBody.layoutParams = params

                val timeParams = messageTime.layoutParams as LinearLayout.LayoutParams
                timeParams.gravity = Gravity.START
                messageTime.layoutParams = timeParams
            }
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean = oldItem == newItem
    }
}
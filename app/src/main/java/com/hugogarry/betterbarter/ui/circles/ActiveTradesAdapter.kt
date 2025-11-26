package com.hugogarry.betterbarter.ui.circles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.hugogarry.betterbarter.BuildConfig
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.data.model.Trade

class ActiveTradesAdapter(
    private val currentUserId: String
) : ListAdapter<Trade, ActiveTradesAdapter.TradeViewHolder>(TradeDiffCallback()) {

    var onProposeClick: ((Trade) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TradeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_trade, parent, false)
        return TradeViewHolder(view)
    }

    override fun onBindViewHolder(holder: TradeViewHolder, position: Int) {
        val trade = getItem(position)
        holder.bind(trade, currentUserId)

        // Only set click listener if it's NOT the user's own trade
        if (trade.proposerId != currentUserId) {
            holder.proposeButton.setOnClickListener {
                onProposeClick?.invoke(trade)
            }
        } else {
            // Remove listener for own trades (or set a different one for editing later)
            holder.proposeButton.setOnClickListener(null)
        }
    }

    class TradeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ownerProfilePic: ImageView = itemView.findViewById(R.id.imageViewOwnerProfile)
        private val ownerName: TextView = itemView.findViewById(R.id.textViewOwnerName)
        private val itemNameAndStock: TextView = itemView.findViewById(R.id.textViewItemNameAndStock)
        private val itemImage: ImageView = itemView.findViewById(R.id.imageViewItem)
        val proposeButton: Button = itemView.findViewById(R.id.buttonProposeTrade)

        private val baseUrl = BuildConfig.BASE_URL.removeSuffix("/api/")

        fun bind(trade: Trade, currentUserId: String) {
            val item = trade.offeredItem

            ownerName.text = trade.proposer.username
            itemNameAndStock.text = "${item?.name ?: "Unknown Item"} (${trade.offeredItemQuantity})"

            // Load owner profile pic
            val profilePicUrl = trade.proposer.profilePictureUrl?.let { "$baseUrl/api/uploads$it" }
            ownerProfilePic.load(profilePicUrl) {
                placeholder(R.drawable.ic_profile)
                error(R.drawable.ic_profile)
                transformations(CircleCropTransformation())
            }

            // Load item image
            val itemPicUrl = item?.imageUrl?.let { "$baseUrl/api/uploads$it" }
            itemImage.load(itemPicUrl) {
                placeholder(R.drawable.ic_launcher_background)
                error(R.drawable.ic_launcher_background)
            }

            if (trade.proposerId == currentUserId) {
                // User created this trade
                proposeButton.text = "Edit Trade Proposal"
                proposeButton.isEnabled = false // Disabled for now
                // Optionally change style to look disabled/secondary
                proposeButton.alpha = 0.5f
            } else {
                // Someone else created this trade
                proposeButton.text = "Apply for Trade"
                proposeButton.isEnabled = false // Disabled for now
                proposeButton.alpha = 0.5f
            }
        }
    }
}

class TradeDiffCallback : DiffUtil.ItemCallback<Trade>() {
    override fun areItemsTheSame(oldItem: Trade, newItem: Trade): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Trade, newItem: Trade): Boolean {
        return oldItem == newItem
    }
}
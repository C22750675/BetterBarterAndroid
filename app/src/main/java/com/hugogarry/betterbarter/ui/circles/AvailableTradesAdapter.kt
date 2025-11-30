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
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.data.model.Trade
import com.hugogarry.betterbarter.util.SessionManager

class AvailableTradesAdapter(
    private val currentUserId: String
) : ListAdapter<Trade, AvailableTradesAdapter.TradeViewHolder>(TradeDiffCallback()) {

    var onProposeClick: ((Trade) -> Unit)? = null
    var onItemClick: ((Trade) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TradeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_trade, parent, false)
        return TradeViewHolder(view)
    }

    override fun onBindViewHolder(holder: TradeViewHolder, position: Int) {
        val trade = getItem(position)
        holder.bind(trade, currentUserId)

        if (trade.proposerId != currentUserId) {
            holder.proposeButton.setOnClickListener {
                onProposeClick?.invoke(trade)
            }
        } else {
            holder.proposeButton.setOnClickListener(null)
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(trade)
        }
    }

    class TradeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ownerProfilePic: ImageView = itemView.findViewById(R.id.imageViewOwnerProfile)
        private val ownerName: TextView = itemView.findViewById(R.id.textViewOwnerName)
        private val itemNameAndStock: TextView = itemView.findViewById(R.id.textViewItemNameAndStock)
        private val itemImage: ImageView = itemView.findViewById(R.id.imageViewItem)
        val proposeButton: Button = itemView.findViewById(R.id.buttonProposeTrade)

        fun bind(trade: Trade, currentUserId: String) {
            val currentApiUrl = SessionManager.getServerUrl()
            val baseUrl = currentApiUrl.removeSuffix("api/")

            val item = trade.offeredItem

            ownerName.text = trade.proposer.username
            itemNameAndStock.text = "${item?.name ?: "Unknown Item"} (${trade.offeredItemQuantity})"

            // Load owner profile pic
            val profilePicUrl = trade.proposer.profilePictureUrl?.let { "${baseUrl}api/uploads$it" }
            ownerProfilePic.load(profilePicUrl) {
                placeholder(R.drawable.ic_profile)
                error(R.drawable.ic_profile)
                transformations(CircleCropTransformation())
            }

            // Load item image
            val itemPicUrl = item?.imageUrl?.let { "${baseUrl}api/uploads$it" }
            itemImage.load(itemPicUrl) {
                placeholder(R.drawable.ic_launcher_background)
                error(R.drawable.ic_launcher_background)
            }

            if (trade.proposerId == currentUserId) {
                proposeButton.text = "Edit Trade Proposal"
                proposeButton.isEnabled = true
                proposeButton.alpha = 0.5f
            } else {
                if (trade.myApplication != null) {
                    proposeButton.text = "Edit Application"
                } else {
                    proposeButton.text = "Apply for Trade"
                }
                proposeButton.isEnabled = true
                proposeButton.alpha = 1.0f
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
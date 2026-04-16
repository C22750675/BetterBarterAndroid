package com.hugogarry.betterbarter.ui.circles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
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
    var onDeleteClick: ((Trade) -> Unit)? = null
    var onItemClick: ((Trade) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TradeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_trade, parent, false)
        return TradeViewHolder(view)
    }

    override fun onBindViewHolder(holder: TradeViewHolder, position: Int) {
        val trade = getItem(position)
        holder.bind(trade, currentUserId)

        holder.proposeButton.setOnClickListener {
            onProposeClick?.invoke(trade)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick?.invoke(trade)
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(trade)
        }
    }

    class TradeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ownerProfilePic: ImageView = itemView.findViewById(R.id.imageViewOwnerProfile)
        private val ownerName: TextView = itemView.findViewById(R.id.textViewOwnerName)
        private val itemName: TextView = itemView.findViewById(R.id.textViewItemName)
        private val itemStock: TextView = itemView.findViewById(R.id.textViewStock)
        private val itemStatus: TextView = itemView.findViewById(R.id.textViewStatus)
        private val itemImage: ImageView = itemView.findViewById(R.id.imageViewItem)
        val proposeButton: Button = itemView.findViewById(R.id.buttonProposeTrade)
        val deleteButton: Button = itemView.findViewById(R.id.buttonDeleteTrade)

        fun bind(trade: Trade, currentUserId: String) {
            val currentApiUrl = SessionManager.getServerUrl()
            val baseUrl = currentApiUrl.removeSuffix("api/")
            val item = trade.offeredItem

            ownerName.text = trade.proposer?.username ?: "Unknown"
            itemName.text = item?.name ?: "Unknown Item"
            itemStock.text = "${trade.offeredItemQuantity} units available"
            itemStatus.text = trade.status.toString().uppercase()

            val profilePicUrl = trade.proposer?.profilePictureUrl?.let { "${baseUrl}api/imageUploads$it" }
            ownerProfilePic.load(profilePicUrl) {
                placeholder(R.drawable.ic_profile)
                error(R.drawable.ic_profile)
                transformations(CircleCropTransformation())
            }

            val itemPicUrl = item?.imageUrl?.let { "${baseUrl}api/imageUploads$it" }
            itemImage.load(itemPicUrl) {
                placeholder(R.drawable.ic_launcher_background)
                error(R.drawable.ic_launcher_background)
            }

            if (trade.proposerId == currentUserId) {
                proposeButton.text = "Edit Trade Proposal"
                deleteButton.isVisible = true
            } else {
                proposeButton.text = if (trade.myApplication != null) "Edit Application" else "Apply for Trade"
                deleteButton.isVisible = false
            }
        }
    }
}

class TradeDiffCallback : DiffUtil.ItemCallback<Trade>() {
    override fun areItemsTheSame(oldItem: Trade, newItem: Trade) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Trade, newItem: Trade) = oldItem == newItem
}
package com.hugogarry.betterbarter.ui.trades

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
import com.hugogarry.betterbarter.data.model.TradeStatus
import com.hugogarry.betterbarter.util.SessionManager

class MyTradesAdapter(
    private val currentUserId: String,
    private val onAction: (Trade, ActionType) -> Unit
) : ListAdapter<Trade, MyTradesAdapter.ViewHolder>(DiffCallback()) {

    enum class ActionType { ACCEPT, REJECT, COMPLETE }

    var onItemClick: ((Trade) -> Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ownerProfilePic: ImageView = itemView.findViewById(R.id.imageViewOwnerProfile)
        val ownerName: TextView = itemView.findViewById(R.id.textViewOwnerName)
        val itemNameAndStock: TextView = itemView.findViewById(R.id.textViewItemNameAndStock)
        val itemImage: ImageView = itemView.findViewById(R.id.imageViewItem)
        val btnAction: Button = itemView.findViewById(R.id.buttonProposeTrade)

        fun bind(trade: Trade) {
            val currentApiUrl = SessionManager.getServerUrl()
            val baseUrl = currentApiUrl.removeSuffix("api/")

            val item = trade.offeredItem

            // 1. Set Text Data
            ownerName.text = trade.proposer.username
            itemNameAndStock.text = "${item?.name ?: "Unknown Item"} (${trade.offeredItemQuantity})\nStatus: ${trade.status.name}"

            // 2. Load Owner Profile Pic
            val profilePicUrl = trade.proposer.profilePictureUrl?.let { "${baseUrl}api/uploads$it" }
            ownerProfilePic.load(profilePicUrl) {
                placeholder(R.drawable.ic_profile)
                error(R.drawable.ic_profile)
                transformations(CircleCropTransformation())
            }

            // 3. Load Item Image
            val itemPicUrl = item?.imageUrl?.let { "${baseUrl}api/uploads$it" }
            itemImage.load(itemPicUrl) {
                placeholder(R.drawable.ic_launcher_background)
                error(R.drawable.ic_launcher_background)
                crossfade(true)
            }

            // 4. Button Logic
            btnAction.isVisible = true
            btnAction.isEnabled = true

            when (trade.status) {
                TradeStatus.pending -> {
                    if (trade.proposerId != currentUserId) {
                        btnAction.text = "Accept Offer"
                        btnAction.setOnClickListener { onAction(trade, ActionType.ACCEPT) }
                    } else {
                        btnAction.text = "Pending..."
                        btnAction.isEnabled = false
                    }
                }
                TradeStatus.accepted -> {
                    btnAction.text = "Mark Complete"
                    btnAction.setOnClickListener { onAction(trade, ActionType.COMPLETE) }
                }
                else -> {
                    // Hide button for COMPLETED or REJECTED statuses
                    btnAction.isVisible = false
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_trade, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trade = getItem(position)
        holder.bind(trade)

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(trade)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Trade>() {
        override fun areItemsTheSame(oldItem: Trade, newItem: Trade) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Trade, newItem: Trade) = oldItem == newItem
    }
}
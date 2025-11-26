package com.hugogarry.betterbarter.ui.trades

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.data.model.Trade
import com.hugogarry.betterbarter.data.model.TradeStatus

// This adapter handles the "Management" of trades (Accept, Complete, Rate)
class MyTradesAdapter(
    private val currentUserId: String, // Need to know who I am to show correct buttons
    private val onAction: (Trade, ActionType) -> Unit
) : ListAdapter<Trade, MyTradesAdapter.ViewHolder>(DiffCallback()) {

    enum class ActionType { ACCEPT, REJECT, COMPLETE, RATE }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.textViewOwnerName) // Reusing existing ID
        val status: TextView = itemView.findViewById(R.id.textViewItemNameAndStock) // Reusing ID
        val btnAction: Button = itemView.findViewById(R.id.buttonProposeTrade) // Reusing ID

        fun bind(trade: Trade) {
            val item = trade.offeredItem?.name ?: "Unknown Item"

            // Visual Logic
            title.text = "Trade for: $item"
            status.text = "Status: ${trade.status.name}"

            btnAction.isVisible = true
            btnAction.isEnabled = true

            // State Machine for Buttons
            when (trade.status) {
                TradeStatus.pending -> {
                    // I can accept if I did NOT propose it
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
                TradeStatus.completed -> {
                    btnAction.text = "Leave Review"
                    btnAction.setOnClickListener { onAction(trade, ActionType.RATE) }
                }
                else -> {
                    btnAction.isVisible = false
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Reusing list_item_trade.xml for speed, but binding different data
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_trade, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Trade>() {
        override fun areItemsTheSame(oldItem: Trade, newItem: Trade) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Trade, newItem: Trade) = oldItem == newItem
    }
}
package com.hugogarry.betterbarter.ui.circles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.data.model.Dispute

class DisputesAdapter(private val onClick: (Dispute) -> Unit) :
    ListAdapter<Dispute, DisputesAdapter.DisputeViewHolder>(DisputeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DisputeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_dispute, parent, false)
        return DisputeViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: DisputeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DisputeViewHolder(itemView: View, val onClick: (Dispute) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val textInitiatorName: TextView = itemView.findViewById(R.id.textInitiatorName)
        private val textRespondentName: TextView = itemView.findViewById(R.id.textRespondentName)
        private val textReason: TextView = itemView.findViewById(R.id.textReason)

        fun bind(dispute: Dispute) {
            textInitiatorName.text = dispute.initiator.username
            textRespondentName.text = dispute.respondent.username
            textReason.text = dispute.reason
            itemView.setOnClickListener { onClick(dispute) }
        }
    }

    class DisputeDiffCallback : DiffUtil.ItemCallback<Dispute>() {
        override fun areItemsTheSame(oldItem: Dispute, newItem: Dispute) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Dispute, newItem: Dispute) = oldItem == newItem
    }
}
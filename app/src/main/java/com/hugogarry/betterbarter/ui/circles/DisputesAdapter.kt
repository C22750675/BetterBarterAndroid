package com.hugogarry.betterbarter.ui.circles

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
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.data.model.Dispute
import com.hugogarry.betterbarter.util.SessionManager

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
        private val textReporterName: TextView = itemView.findViewById(R.id.textReporterName)
        private val textOtherPartyName: TextView = itemView.findViewById(R.id.textOtherPartyName)
        private val imageReporter: ImageView = itemView.findViewById(R.id.imageReporter)
        private val imageOtherParty: ImageView = itemView.findViewById(R.id.imageOtherParty)
        private val textReason: TextView = itemView.findViewById(R.id.textReason)
        private val textViewStatus: TextView = itemView.findViewById(R.id.textViewStatus)

        fun bind(dispute: Dispute) {
            // Retrieve dynamic baseUrl to properly load images with Coil
            val currentApiUrl = SessionManager.getServerUrl()
            val baseUrl = currentApiUrl.removeSuffix("api/")

            // Determine who reported the dispute based on IDs
            val isProposerReporter = dispute.trade?.proposerId == dispute.reporterId

            // Extract the whole User object from the nested Trade object
            val reporter = if (isProposerReporter) dispute.trade.proposer else dispute.trade?.recipient
            val otherParty = if (isProposerReporter) dispute.trade.recipient else dispute.trade?.proposer

            // Set Data
            textReporterName.text = reporter?.username ?: "Reporter"
            textOtherPartyName.text = otherParty?.username ?: "Other Party"
            textReason.text = dispute.description
            textViewStatus.text = dispute.status.uppercase()

            // Load Reporter Profile Picture
            val reporterPicUrl = reporter?.profilePictureUrl?.let { "${baseUrl}api/uploads$it" }
            imageReporter.load(reporterPicUrl) {
                placeholder(R.drawable.ic_profile)
                error(R.drawable.ic_profile)
                transformations(CircleCropTransformation())
            }

            // Load Other Party Profile Picture
            val otherPartyPicUrl = otherParty?.profilePictureUrl?.let { "${baseUrl}api/uploads$it" }
            imageOtherParty.load(otherPartyPicUrl) {
                placeholder(R.drawable.ic_profile)
                error(R.drawable.ic_profile)
                transformations(CircleCropTransformation())
            }

            itemView.setOnClickListener { onClick(dispute) }
        }
    }

    class DisputeDiffCallback : DiffUtil.ItemCallback<Dispute>() {
        override fun areItemsTheSame(oldItem: Dispute, newItem: Dispute) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Dispute, newItem: Dispute) = oldItem == newItem
    }
}
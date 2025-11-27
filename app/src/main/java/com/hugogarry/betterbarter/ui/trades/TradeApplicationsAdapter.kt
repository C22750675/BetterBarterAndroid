package com.hugogarry.betterbarter.ui.trades

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
import com.hugogarry.betterbarter.data.model.TradeApplication

class TradeApplicationsAdapter(
    private val onAccept: (TradeApplication) -> Unit,
    private val onDecline: (TradeApplication) -> Unit
) : ListAdapter<TradeApplication, TradeApplicationsAdapter.ApplicationViewHolder>(ApplicationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_trade_application, parent, false)
        return ApplicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        val application = getItem(position)
        holder.bind(application, onAccept, onDecline)
    }

    class ApplicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val applicantImage: ImageView = itemView.findViewById(R.id.imageViewApplicantProfile)
        private val applicantName: TextView = itemView.findViewById(R.id.textViewApplicantName)
        private val applicantReputation: TextView = itemView.findViewById(R.id.textViewApplicantReputation)
        private val itemImage: ImageView = itemView.findViewById(R.id.imageViewOfferedItem)
        private val itemName: TextView = itemView.findViewById(R.id.textViewOfferedItemName)
        private val itemValue: TextView = itemView.findViewById(R.id.textViewOfferedValue)
        private val message: TextView = itemView.findViewById(R.id.textViewMessage)
        private val acceptButton: Button = itemView.findViewById(R.id.buttonAccept)
        private val declineButton: Button = itemView.findViewById(R.id.buttonDecline)

        private val baseUrl = BuildConfig.BASE_URL.removeSuffix("/api/")

        fun bind(
            app: TradeApplication,
            onAccept: (TradeApplication) -> Unit,
            onDecline: (TradeApplication) -> Unit
        ) {
            // Applicant Info
            val applicant = app.applicant
            applicantName.text = applicant?.username ?: "Unknown User"
            applicantReputation.text = "Reputation: ${applicant?.reputationScore ?: 0.0} ★"

            val profileUrl = applicant?.profilePictureUrl?.let { "$baseUrl/api/uploads$it" }
            applicantImage.load(profileUrl) {
                placeholder(R.drawable.ic_profile)
                error(R.drawable.ic_profile)
                transformations(CircleCropTransformation())
            }

            // Item Info
            val item = app.offeredItem
            itemName.text = "Offered: ${item?.name ?: "Unknown Item"} (Qty: ${app.offeredItemQuantity})"
            itemValue.text = "Est. Value: €${item?.estimatedValue ?: 0.0}"

            val itemUrl = item?.imageUrl?.let { "$baseUrl/api/uploads$it" }
            itemImage.load(itemUrl) {
                placeholder(R.drawable.ic_launcher_background)
                error(R.drawable.ic_launcher_background)
                crossfade(true)
            }

            // Message
            if (!app.message.isNullOrBlank()) {
                message.text = "\"${app.message}\""
                message.visibility = View.VISIBLE
            } else {
                message.visibility = View.GONE
            }

            // Buttons
            acceptButton.setOnClickListener { onAccept(app) }
            declineButton.setOnClickListener { onDecline(app) }
        }
    }

    class ApplicationDiffCallback : DiffUtil.ItemCallback<TradeApplication>() {
        override fun areItemsTheSame(oldItem: TradeApplication, newItem: TradeApplication): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TradeApplication, newItem: TradeApplication): Boolean {
            return oldItem == newItem
        }
    }
}
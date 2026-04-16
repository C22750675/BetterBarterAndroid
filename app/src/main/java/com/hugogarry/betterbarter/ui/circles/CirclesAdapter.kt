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
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.data.model.Circle
import com.hugogarry.betterbarter.util.SessionManager

class CirclesAdapter(
    private val showJoinButton: Boolean = false
) : ListAdapter<Circle, CirclesAdapter.CircleViewHolder>(CircleDiffCallback()) {

    var onItemClick: ((Circle) -> Unit)? = null
    var onJoinClick: ((Circle) -> Unit)? = null

    class CircleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val circleName: TextView = itemView.findViewById(R.id.textViewCircleName)
        private val circleMembers: TextView = itemView.findViewById(R.id.textViewCircleMembers)
        private val circleIcon: ImageView = itemView.findViewById(R.id.imageViewCircleIcon)
        val joinButton: Button = itemView.findViewById(R.id.buttonJoinCircle)

        fun bind(circle: Circle, showJoin: Boolean) {
            circleName.text = circle.name
            circleMembers.text = "${circle.memberCount} Members"

            joinButton.isVisible = showJoin

            // Construct the full image URL
            val currentApiUrl = SessionManager.getServerUrl()
            val baseUrl = currentApiUrl.removeSuffix("api/")

            // Backend returns 'imageUrl' with a leading slash
            // We ensure we don't end up with triple slashes (e.g. uploads///image.jpg)
            val path = circle.imageUrl?.removePrefix("/")
            val fullImageUrl = path?.let { "${baseUrl}api/imageUploads/$it" }

            // Clear the XML tint if we are loading a real photo.
            // list_item_circle.xml has a default tint for the icon that will cover the photo.
            if (fullImageUrl != null) {
                circleIcon.imageTintList = null
            }

            circleIcon.load(fullImageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_circles)
                error(R.drawable.ic_circles)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CircleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_circle, parent, false)
        return CircleViewHolder(view)
    }

    override fun onBindViewHolder(holder: CircleViewHolder, position: Int) {
        val circle = getItem(position)
        holder.bind(circle, showJoinButton)

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(circle)
        }

        holder.joinButton.setOnClickListener {
            onJoinClick?.invoke(circle)
        }
    }
}

class CircleDiffCallback : DiffUtil.ItemCallback<Circle>() {
    override fun areItemsTheSame(oldItem: Circle, newItem: Circle): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Circle, newItem: Circle): Boolean {
        return oldItem == newItem
    }
}
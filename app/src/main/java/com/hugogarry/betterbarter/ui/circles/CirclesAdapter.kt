package com.hugogarry.betterbarter.ui.circles

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
import com.hugogarry.betterbarter.data.model.Circle

class CirclesAdapter(
    private val showJoinButton: Boolean = false
) : ListAdapter<Circle, CirclesAdapter.CircleViewHolder>(CircleDiffCallback()) {

    var onItemClick: ((Circle) -> Unit)? = null
    var onJoinClick: ((Circle) -> Unit)? = null

    class CircleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val circleName: TextView = itemView.findViewById(R.id.textViewCircleName)
        private val circleMembers: TextView = itemView.findViewById(R.id.textViewCircleMembers)
        private val circleReputation: TextView = itemView.findViewById(R.id.textViewCircleReputation)
        val joinButton: Button = itemView.findViewById(R.id.buttonJoinCircle)

        fun bind(circle: Circle, showJoin: Boolean) {
            circleName.text = circle.name
            circleMembers.text = "${circle.memberCount} Members"
            // Using a star emoji for reputation
            circleReputation.text = "%.1f ★".format(circle.reputationScore)

            joinButton.isVisible = showJoin
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
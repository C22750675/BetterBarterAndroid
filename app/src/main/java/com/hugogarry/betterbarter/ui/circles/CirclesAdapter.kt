package com.hugogarry.betterbarter.ui.circles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.data.model.Circle

class CirclesAdapter : ListAdapter<Circle, CirclesAdapter.CircleViewHolder>(CircleDiffCallback()) {

    class CircleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val circleName: TextView = itemView.findViewById(R.id.textViewCircleName)
        private val circleMembers: TextView = itemView.findViewById(R.id.textViewCircleMembers)
        private val circleReputation: TextView = itemView.findViewById(R.id.textViewCircleReputation)

        fun bind(circle: Circle) {
            circleName.text = circle.name
            circleMembers.text = "${circle.memberCount} Members"
            // Using a star emoji for reputation
            circleReputation.text = "%.1f ★".format(circle.reputationScore)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CircleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_circle, parent, false)
        return CircleViewHolder(view)
    }

    override fun onBindViewHolder(holder: CircleViewHolder, position: Int) {
        holder.bind(getItem(position))
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
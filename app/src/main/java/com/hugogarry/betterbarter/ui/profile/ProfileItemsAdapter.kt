package com.hugogarry.betterbarter.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.data.model.Item
import com.hugogarry.betterbarter.util.SessionManager

class ProfileItemsAdapter : ListAdapter<Item, ProfileItemsAdapter.ItemViewHolder>(ItemDiffCallback()) {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemName: TextView = itemView.findViewById(R.id.textViewItemName)
        private val itemDescription: TextView = itemView.findViewById(R.id.textViewItemDescription)
        private val itemImage: ImageView = itemView.findViewById(R.id.imageViewItem)
        // Added the stock view reference
        private val itemStock: TextView = itemView.findViewById(R.id.textViewItemStock)

        fun bind(item: Item) {
            itemName.text = item.name
            itemDescription.text = item.description

            // Display the stock count clearly
            itemStock.text = "In Stock: ${item.stock}"

            val currentApiUrl = SessionManager.getServerUrl()
            val baseUrl = currentApiUrl.removeSuffix("api/")
            val fullImageUrl = item.imageUrl?.let { "${baseUrl}api/uploads$it" }

            itemImage.load(fullImageUrl) {
                placeholder(R.drawable.ic_profile)
                error(R.drawable.ic_profile)
                crossfade(true)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_profile, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ItemDiffCallback : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem == newItem
    }
}
package com.sekhgmainuddin.timeshare.ui.home.profile.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.Reel
import com.sekhgmainuddin.timeshare.databinding.UploadedReelsLayoutRvBinding

class ProfileReelsAdapter(val context: Context) :
    ListAdapter<Reel, ProfileReelsAdapter.ReelViewHolder>(ReelDiffCallback()) {

    private class ReelDiffCallback() : DiffUtil.ItemCallback<Reel>() {
        override fun areItemsTheSame(oldItem: Reel, newItem: Reel): Boolean {
            return oldItem.reelId == newItem.reelId
        }

        override fun areContentsTheSame(oldItem: Reel, newItem: Reel): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReelViewHolder {
        return ReelViewHolder(UploadedReelsLayoutRvBinding.inflate(LayoutInflater.from(context), parent, false), context)
    }

    override fun onBindViewHolder(holder: ReelViewHolder, position: Int) {
        holder.loadView(currentList[position])
        holder.itemView.setOnLongClickListener {
            Toast.makeText(context, "Item $position Pressed", Toast.LENGTH_SHORT).show()
            true
        }
    }

    class ReelViewHolder(val binding: UploadedReelsLayoutRvBinding, val context: Context) :
        RecyclerView.ViewHolder(binding.root) {
        fun loadView(reel: Reel) {
            binding.apply {
                reelViews.text = reel.reelViewsCount.toString()
                Glide.with(context).load(reel.reelThumbnail).placeholder(R.color.black).into(reelThumbnail)
            }
        }
    }

}
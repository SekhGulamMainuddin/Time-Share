package com.sekhgmainuddin.timeshare.ui.home.notification.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.NotificationItem
import com.sekhgmainuddin.timeshare.databinding.FollowUserRvBinding
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class NotificationsAdapter(val context: Context, val onClickNotify: OnClickNotify): ListAdapter<NotificationItem, NotificationsAdapter.NotificationViewHolder>(NotificationDiffUtil()) {

    private class NotificationDiffUtil : DiffUtil.ItemCallback<NotificationItem>() {
        override fun areItemsTheSame(
            oldItem: NotificationItem,
            newItem: NotificationItem
        ): Boolean {
            return oldItem==newItem
        }

        override fun areContentsTheSame(
            oldItem: NotificationItem,
            newItem: NotificationItem
        ): Boolean {
            return oldItem==newItem
        }

    }

    class NotificationViewHolder(val binding: FollowUserRvBinding, val context: Context) : RecyclerView.ViewHolder(binding.root){

        val followButton= binding.followButton

        fun bind(data: Map<String,String>) {
            binding.apply {

                profileName.text= data["profileName"]
                Glide.with(context).load(data["profileImage"]).placeholder(R.drawable.default_profile_pic).into(profileImage)
                followButton.text= "ACCEPT"
                profileBio.text= ""
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder(FollowUserRvBinding.inflate(LayoutInflater.from(context), parent, false), context)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item= currentList[position]
        if (item.type=="FRIENDREQUEST"){
            val data= Json.decodeFromString<Map<String,String>>(item.body)
            holder.bind(data)
            holder.followButton.setOnClickListener {
                data["profileId"]?.let { it1 -> onClickNotify.friendRequestAccepted(it1) }
                holder.followButton.text= "Accepted"
            }
        }
    }

    interface OnClickNotify{
        fun friendRequestAccepted(id: String)
    }

}
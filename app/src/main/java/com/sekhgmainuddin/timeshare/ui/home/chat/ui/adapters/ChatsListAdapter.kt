package com.sekhgmainuddin.timeshare.ui.home.chat.ui.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.db.entities.RecentProfileChatsEntity
import com.sekhgmainuddin.timeshare.utils.Utils.getTimeAgo

class ChatsListAdapter(val context: Context, val onClicked: onClicked): ListAdapter<RecentProfileChatsEntity, ChatsListAdapter.ChatsListViewHolder>(
    ChatListDiffCallBack()
) {

    private class ChatListDiffCallBack: DiffUtil.ItemCallback<RecentProfileChatsEntity>() {
        override fun areItemsTheSame(oldItem: RecentProfileChatsEntity, newItem: RecentProfileChatsEntity): Boolean {
            return oldItem==newItem
        }

        override fun areContentsTheSame(oldItem: RecentProfileChatsEntity, newItem: RecentProfileChatsEntity): Boolean {
            return true
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsListViewHolder {
        return ChatsListViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_profiles_rv, parent, false))
    }

    override fun onBindViewHolder(holder: ChatsListViewHolder, position: Int) {
        val currentItem= currentList[position]
        Glide.with(context).load(currentItem.profileImageUrl).placeholder(R.drawable.default_profile_pic).into(holder.profileImage)
        holder.profileRecentMessage.text= currentItem.recentMessage
        holder.unseenMsgCount.text= currentItem.numberOfUnseenMessages.toString()
        holder.unseenMsgCount.isVisible = currentItem.numberOfUnseenMessages == 0
        holder.profilename.text= currentItem.profileName
        holder.lastMessageTime.text= currentItem.lastMessageTime.getTimeAgo()
        holder.itemView.setOnClickListener {
            onClicked.onProfileClicked(currentItem.profileId, currentItem.profileName, currentItem.profileImageUrl, currentItem.typeGroup)
        }
    }

    inner class ChatsListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage= itemView.findViewById<ShapeableImageView>(R.id.profileImage)
        val profilename= itemView.findViewById<TextView>(R.id.profileName)
        val profileRecentMessage= itemView.findViewById<TextView>(R.id.profileRecentMessage)
        val unseenMsgCount= itemView.findViewById<MaterialButton>(R.id.numberOfUnreadMessages)
        val lastMessageTime= itemView.findViewById<TextView>(R.id.lastMessageTime)
    }

}
interface onClicked{
    fun onProfileClicked(profileId: String, profileName: String, profileImageUrl: String, isGroup: Boolean)
}
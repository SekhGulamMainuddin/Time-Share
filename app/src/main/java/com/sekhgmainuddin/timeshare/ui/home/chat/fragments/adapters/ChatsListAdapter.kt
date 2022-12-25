package com.sekhgmainuddin.timeshare.ui.home.chat.fragments.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.sekhgmainuddin.timeshare.R

class ChatsListAdapter(val context: Context): ListAdapter<String, ChatsListAdapter.ChatsListViewHolder>(
    ChatsDiffCallBack()
) {

    private class ChatsDiffCallBack: DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem==newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem.equals(newItem, true)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsListViewHolder {
        return ChatsListViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_profiles_vh, parent, false))
    }

    override fun onBindViewHolder(holder: ChatsListViewHolder, position: Int) {
        holder.msgHeaderTV.isVisible = position==0
        holder.unseenMsg.text= position.toString()
        holder.unseenMsg.isVisible = position%2==0
        holder.msg.text= currentList[position]
    }

    inner class ChatsListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val msgHeaderTV= itemView.findViewById<TextView>(R.id.messagesTV)
        val msg= itemView.findViewById<TextView>(R.id.profileName)
        val unseenMsg= itemView.findViewById<MaterialButton>(R.id.numberOfUnreadMessages)
    }

}
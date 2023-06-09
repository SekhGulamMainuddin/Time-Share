package com.sekhgmainuddin.timeshare.ui.home.chat.backend.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.db.entities.ChatEntity
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.ui.home.chat.backend.adapters.diffutilsmessage.ChatsDiffCallBack
import com.sekhgmainuddin.timeshare.ui.home.chat.backend.adapters.viewholders.ChatsViewHolder
import com.sekhgmainuddin.timeshare.ui.home.chat.backend.adapters.viewholders.GroupChatsViewHolder
import com.sekhgmainuddin.timeshare.utils.Constants

class GroupChatsAdapter(val userMap: Map<String,User>, val context: Context, val firebaseUserId: String, val showDialog: (ChatEntity) -> Unit) :
    ListAdapter<ChatEntity, GroupChatsViewHolder>(
        ChatsDiffCallBack()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupChatsViewHolder {
        return if (viewType == Constants.MSG_BY_USER)
            GroupChatsViewHolder(
                LayoutInflater.from(context).inflate(R.layout.chats_layout_user_end, parent, false), context, firebaseUserId
            )
        else
            GroupChatsViewHolder(
                LayoutInflater.from(context)
                    .inflate(R.layout.chats_layout_opposite_end, parent, false), context, firebaseUserId
            )
    }

    override fun onBindViewHolder(holder: GroupChatsViewHolder, position: Int) {
        val item = currentList[position]
        holder.bind(item, userMap[item.senderId]!!)
        holder.itemView.setOnLongClickListener{
            showDialog.invoke(item)
            return@setOnLongClickListener true
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (currentList[position].senderId == firebaseUserId)
            Constants.MSG_BY_USER
        else Constants.MSG_BY_OPPOSITE
    }

}

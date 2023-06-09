package com.sekhgmainuddin.timeshare.ui.home.chat.backend.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.db.entities.ChatEntity
import com.sekhgmainuddin.timeshare.ui.home.chat.backend.adapters.diffutilsmessage.ChatsDiffCallBack
import com.sekhgmainuddin.timeshare.ui.home.chat.backend.adapters.viewholders.ChatsViewHolder
import com.sekhgmainuddin.timeshare.utils.Constants.MSG_BY_OPPOSITE
import com.sekhgmainuddin.timeshare.utils.Constants.MSG_BY_USER

class ChatsAdapter(val context: Context, val firebaseUserId: String, val showDialog: (ChatEntity) -> Unit) :
    ListAdapter<ChatEntity, ChatsViewHolder>(
        ChatsDiffCallBack()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
        return if (viewType == MSG_BY_USER)
            ChatsViewHolder(
                LayoutInflater.from(context).inflate(R.layout.chats_layout_user_end, parent, false), context, firebaseUserId
            )
        else
            ChatsViewHolder(
                LayoutInflater.from(context)
                    .inflate(R.layout.chats_layout_opposite_end, parent, false), context, firebaseUserId
            )
    }

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        val item = currentList[position]
        holder.bind(item)
        holder.itemView.setOnLongClickListener{
            showDialog.invoke(item)
            return@setOnLongClickListener true
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (currentList[position].senderId == firebaseUserId)
            MSG_BY_USER
        else MSG_BY_OPPOSITE
    }

}

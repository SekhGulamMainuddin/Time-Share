package com.sekhgmainuddin.timeshare.ui.home.chat.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.db.entities.ChatEntity
import com.sekhgmainuddin.timeshare.utils.Constants.MSG_BY_OPPOSITE
import com.sekhgmainuddin.timeshare.utils.Constants.MSG_BY_USER
import com.sekhgmainuddin.timeshare.utils.Constants.MSG_OLD
import com.sekhgmainuddin.timeshare.utils.Constants.MSG_RECEIVED
import com.sekhgmainuddin.timeshare.utils.Constants.MSG_SEEN
import com.sekhgmainuddin.timeshare.utils.Constants.MSG_SENT
import com.sekhgmainuddin.timeshare.utils.Constants.MSG_TYPE_FILE
import com.sekhgmainuddin.timeshare.utils.Constants.MSG_TYPE_IMAGE
import com.sekhgmainuddin.timeshare.utils.Constants.MSG_TYPE_IMAGE_AND_MESSAGE
import com.sekhgmainuddin.timeshare.utils.Constants.MSG_TYPE_MESSAGE
import com.sekhgmainuddin.timeshare.utils.Constants.MSG_TYPE_PDF

class ChatsAdapter(val context: Context, val chatProfileImage: String, val firebaseUserId: String) : ListAdapter<ChatEntity, ChatsAdapter.ChatsViewHolder>(
    ChatsDiffCallBack()
) {

    private class ChatsDiffCallBack: DiffUtil.ItemCallback<ChatEntity>() {
        override fun areItemsTheSame(oldItem: ChatEntity, newItem: ChatEntity): Boolean {
            return oldItem==newItem
        }

        override fun areContentsTheSame(oldItem: ChatEntity, newItem: ChatEntity): Boolean {
            return oldItem.message == newItem.message && oldItem.document == newItem.document
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
        return if (viewType == MSG_BY_USER)
            ChatsViewHolder(LayoutInflater.from(context).inflate(R.layout.chats_layout_user_end, parent, false))
        else
            ChatsViewHolder(LayoutInflater.from(context).inflate(R.layout.chats_layout_opposite_end, parent, false))
    }

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    override fun getItemViewType(position: Int): Int {
        return if (currentList[position].senderId == firebaseUserId)
            MSG_BY_USER
        else MSG_BY_OPPOSITE
    }

    inner class ChatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val messageTV= itemView.findViewById<TextView>(R.id.senderMessage)
        val messageStatus= itemView.findViewById<ImageButton>(R.id.messageStatus)

        fun bind(chatEntity: ChatEntity){
            when(chatEntity.type){
                MSG_TYPE_MESSAGE-> {
                    messageTV.text= chatEntity.message
                }
                MSG_TYPE_IMAGE-> {

                }
                MSG_TYPE_IMAGE_AND_MESSAGE-> {

                }
                MSG_TYPE_PDF-> {

                }
                MSG_TYPE_FILE-> {

                }
            }
            if (chatEntity.senderId!= firebaseUserId){
                messageStatus.isVisible= false
                when(chatEntity.messageStatus){
                    MSG_OLD-> {
                        messageStatus.visibility= View.GONE
                    }
                    MSG_SENT-> {
                        messageStatus.imageTintList= ColorStateList.valueOf(context.getColor(R.color.profile_text_color))
                    }
                    MSG_RECEIVED-> {
                        messageStatus.imageTintList= ColorStateList.valueOf(context.getColor(R.color.orange))
                    }
                    MSG_SEEN-> {
                        Glide.with(context).load(chatProfileImage).placeholder(R.drawable.default_profile_pic).into(messageStatus)
                    }
                }
            }else{
                messageStatus.isVisible= true
                messageStatus.setImageResource(R.drawable.ic_baseline_check_circle_24)
            }
        }

    }

}
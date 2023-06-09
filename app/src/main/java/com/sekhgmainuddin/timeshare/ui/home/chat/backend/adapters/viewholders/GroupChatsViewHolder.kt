package com.sekhgmainuddin.timeshare.ui.home.chat.backend.adapters.viewholders

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.db.entities.ChatEntity
import com.sekhgmainuddin.timeshare.data.modals.User

class GroupChatsViewHolder(itemView: View, context: Context, firebaseUserId: String) : ChatsViewHolder(itemView, context,
    firebaseUserId
) {

    val senderName= itemView.findViewById<TextView>(R.id.senderName)
    val senderImage= itemView.findViewById<ShapeableImageView>(R.id.senderImage)

    fun bind(chatEntity: ChatEntity, user: User) {
        super.bind(chatEntity)
        if (chatEntity.senderId!=firebaseUserId){
            senderName.isVisible= true
            senderImage.isVisible= true
            Glide.with(context).load(user.imageUrl).placeholder(R.drawable.default_profile_pic).into(senderImage)
            senderName.text= user.name
        }
    }


}
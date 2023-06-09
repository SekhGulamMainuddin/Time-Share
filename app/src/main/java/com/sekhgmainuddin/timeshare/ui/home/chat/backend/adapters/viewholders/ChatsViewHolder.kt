package com.sekhgmainuddin.timeshare.ui.home.chat.backend.adapters.viewholders

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.db.entities.ChatEntity
import com.sekhgmainuddin.timeshare.utils.Utils.getMessageTIme
import com.sekhgmainuddin.timeshare.utils.enums.MessageStatus
import com.sekhgmainuddin.timeshare.utils.enums.MessageType

open class ChatsViewHolder(itemView: View, val context: Context, val firebaseUserId: String) : RecyclerView.ViewHolder(itemView) {
    val messageTV = itemView.findViewById<TextView>(R.id.textMessage)
    val messageStatus = itemView.findViewById<ImageButton>(R.id.messageStatus)
    val messageTime = itemView.findViewById<TextView>(R.id.messageTime)
    val messageImage = itemView.findViewById<ImageView>(R.id.imageMessage)

    open fun bind(chatEntity: ChatEntity) {
        messageImage.isVisible= false
        when (MessageType.valueOf(chatEntity.type)) {
            MessageType.TEXT -> {
                messageTV.text = chatEntity.message
            }
            MessageType.IMAGE -> {
                messageImage.isVisible= true
                messageTV.text= chatEntity.message
                if (chatEntity.message.isEmpty())
                    messageTV.isVisible= false
                Glide.with(context).load(chatEntity.document).into(messageImage)
            }
            MessageType.GIF -> {
                messageImage.isVisible= true
                messageTV.isVisible= false
                Glide.with(context).load(chatEntity.document).into(messageImage)
            }
            MessageType.VIDEO -> {

            }
            MessageType.PDF -> {
                messageTV.isVisible= false
                messageImage.isVisible= true
                Glide.with(context).load(chatEntity.message).placeholder(R.drawable.pdf).into(messageImage)
            }
            MessageType.DOCUMENT -> {

            }
            MessageType.AUDIO -> {}
            MessageType.DOCX -> {}
        }
        messageTime.text = chatEntity.time.getMessageTIme(context)
        if (chatEntity.senderId == firebaseUserId) {
            messageStatus.isVisible = true
            when (MessageStatus.valueOf(chatEntity.messageStatus)) {
                MessageStatus.MSG_SENT -> {
                    Glide.with(context).load(R.drawable.single_tick).into(messageStatus)
                    messageStatus.imageTintList =
                        ColorStateList.valueOf(context.getColor(R.color.white))
                }
                MessageStatus.MSG_RECEIVED -> {
                    Glide.with(context).load(R.drawable.double_tick).into(messageStatus)
                    messageStatus.imageTintList =
                        ColorStateList.valueOf(context.getColor(R.color.white))
                }
                MessageStatus.MSG_SEEN -> {
                    Glide.with(context).load(R.drawable.double_tick).into(messageStatus)
                    messageStatus.imageTintList =
                        ColorStateList.valueOf(context.getColor(R.color.message_seen))
                }
                MessageStatus.MSG_OLD -> {
                    messageStatus.visibility = View.GONE
                }
            }
        }
    }
}
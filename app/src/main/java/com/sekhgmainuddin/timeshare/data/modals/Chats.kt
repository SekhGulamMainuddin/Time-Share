package com.sekhgmainuddin.timeshare.data.modals

import com.google.firebase.database.PropertyName

data class Chats(
    @PropertyName("chatId") val chatId: String,
    @PropertyName("senderId") val senderId: String,
    @PropertyName("type") val type: Int,
    @PropertyName("message") val message: String,
    @PropertyName("document") val document: String? = null,
    @PropertyName("time") val time: Long,
    @PropertyName("messageStatus") val messageStatus: Int
){
    constructor() : this("","",0,"","",0,0)
}
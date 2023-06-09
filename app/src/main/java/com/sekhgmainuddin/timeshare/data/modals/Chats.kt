package com.sekhgmainuddin.timeshare.data.modals

data class Chats(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val type: String = "",
    val message: String = "",
    val document: String? = null,
    val time: Long = 0L,
    var messageStatus: String = ""
)
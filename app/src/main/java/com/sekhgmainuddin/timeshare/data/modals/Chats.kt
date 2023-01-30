package com.sekhgmainuddin.timeshare.data.modals

data class Chats(
    val chatId: String = "",
    val senderId: String = "",
    val type: Int = 0,
    val message: String = "",
    val document: String? = null,
    val time: Long = 0L,
    val messageStatus: Int = 0
)
package com.sekhgmainuddin.timeshare.data.modals

import com.sekhgmainuddin.timeshare.utils.enums.MessageStatus
import com.sekhgmainuddin.timeshare.utils.enums.MessageType

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
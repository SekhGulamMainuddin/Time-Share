package com.sekhgmainuddin.timeshare.data.modals

data class RecentGroupChats(
    val profileId: String = "",
    val recentMessage: String = "",
    val lastMessageTime: Long = 0L,
    val numberOfUnseenMessages: Int = 0,
    val group: Boolean = false,
    val byProfile: String = ""
)
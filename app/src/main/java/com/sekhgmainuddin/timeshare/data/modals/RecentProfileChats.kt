package com.sekhgmainuddin.timeshare.data.modals

data class RecentProfileChats(
    val profileId: String = "",
    val recentMessage: String = "",
    val lastMessageTime: Long = 0L,
    val numberOfUnseenMessages: Int = 0
)
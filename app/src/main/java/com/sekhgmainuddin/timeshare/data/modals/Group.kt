package com.sekhgmainuddin.timeshare.data.modals

data class Group(
    val groupId: String = "",
    val groupName: String = "",
    val groupImageUrl: String = "",
    val groupDesc: String = "",
    val groupMembers: List<String> = emptyList(),
    val notificationToken: String = ""
)
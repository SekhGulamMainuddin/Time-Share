package com.sekhgmainuddin.timeshare.data.modals

import com.google.firebase.database.PropertyName

data class RecentProfileChats(
    @PropertyName("profileId") val profileId: String,
    @PropertyName("recentMessage") val recentMessage: String,
    @PropertyName("lastMessageTime") val lastMessageTime: Long,
    @PropertyName("numberOfUnseenMessages") val numberOfUnseenMessages: Int
) {
    constructor() :  this("","",0,0)
}
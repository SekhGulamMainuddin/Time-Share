package com.sekhgmainuddin.timeshare.data.modals

import com.google.firebase.database.PropertyName

data class UserWithFriendFollowerAndFollowingLists (
    @PropertyName("name") val name: String,
    @PropertyName("email") val email: String = "",
    @PropertyName("phone") val phone: String = "",
    @PropertyName("imageUrl") val imageUrl: String,
    @PropertyName("bio") val bio: String? = null,
    @PropertyName("interests") val interests: ArrayList<String>? = null,
    @PropertyName("location") val location: String? = null,
    @PropertyName("activeStatus") val activeStatus: Long = 0L,
    @PropertyName("friends") val friends: Map<String, User> = emptyMap(),
    @PropertyName("followers") val followers: Map<String, User> = emptyMap(),
    @PropertyName("following") val following: Map<String, User> = emptyMap(),
    @PropertyName("isVerified") val isVerified: Boolean = false,

    ) {
    constructor() : this ("","","","","",null, "",0L, emptyMap(), emptyMap(), emptyMap(),false)
}
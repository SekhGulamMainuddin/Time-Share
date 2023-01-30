package com.sekhgmainuddin.timeshare.data.modals

data class UserWithFriendFollowerAndFollowingLists (
    val name: String = "",
    var userId: String= "",
    val email: String = "",
    val phone: String = "",
    val imageUrl: String = "",
    val bio: String? = null,
    val interests: ArrayList<String>? = null,
    val location: String? = null,
    val activeStatus: Long = 0L,
    val friends: Map<String, User> = emptyMap(),
    val followers: Map<String, User> = emptyMap(),
    val following: Map<String, User> = emptyMap(),
    val isVerified: Boolean = false,

)
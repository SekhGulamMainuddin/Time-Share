package com.sekhgmainuddin.timeshare.data.modals

data class LikeWithProfile (
    val profileId: String= "",
    val profileName: String= "",
    val profileImage: String= "",
    val likedTime: Long= 0L,
)
package com.sekhgmainuddin.timeshare.data.modals

data class CommentWithProfile (
    val profileId: String= "",
    val profileName: String= "",
    val profileImage: String= "",
    val comment: String= "",
    val commentTime: Long= 0L,
)
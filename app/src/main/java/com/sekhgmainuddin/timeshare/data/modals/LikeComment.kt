package com.sekhgmainuddin.timeshare.data.modals

import com.google.firebase.database.PropertyName

data class LikeComment (
    @PropertyName("liked") val liked: Boolean= false,
    @PropertyName("comment") val comment: String= "",
    @PropertyName("commentTime") val commentTime: Long= 0L,
    @PropertyName("likedTime") val likedTime: Long= 0L,
) {
    constructor() : this (false,"",0L,0L)
}
package com.sekhgmainuddin.timeshare.data.modals

import java.io.Serializable

@kotlinx.serialization.Serializable
data class LikeComment (
    val liked: Boolean= false,
    val comment: String= "",
    val commentTime: Long= 0L,
    val likedTime: Long= 0L,
) : Serializable
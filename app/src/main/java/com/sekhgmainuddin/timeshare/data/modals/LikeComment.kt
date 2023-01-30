package com.sekhgmainuddin.timeshare.data.modals

import com.google.firebase.database.PropertyName
import java.io.Serializable

data class LikeComment (
    val liked: Boolean= false,
    val comment: String= "",
    val commentTime: Long= 0L,
    val likedTime: Long= 0L,
) : Serializable
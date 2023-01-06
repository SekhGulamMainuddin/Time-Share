package com.sekhgmainuddin.timeshare.data.modals

import com.google.firebase.database.PropertyName

data class LikeComment (
    @PropertyName("liked") val liked: Boolean= false,
    @PropertyName("comment") val comment: String= ""
) {
    constructor() : this (false,"")
}
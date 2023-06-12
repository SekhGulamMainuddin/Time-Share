package com.sekhgmainuddin.timeshare.data.modals

import android.graphics.Color

@kotlinx.serialization.Serializable
data class Status (
    val statusId: String = "",
    val type: String = "EMPTY",
    val urlOrText: String = "",
    val statusUploadTime: Long = 0L,
    val thumbnail: String = "",
    val background: Int = Color.BLACK,
    val captions : String = "",
    val statusSeenIds: ArrayList<String> = arrayListOf()
)
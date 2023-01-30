package com.sekhgmainuddin.timeshare.data.modals

import com.sekhgmainuddin.timeshare.utils.Constants.TYPE_IMAGE

@kotlinx.serialization.Serializable
data class Status (
    val statusId: String = "",
    val imageOrVideo: Int = TYPE_IMAGE,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val text: String = "",
    val statusUploadTime: Long = 0L,
)
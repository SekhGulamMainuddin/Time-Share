package com.sekhgmainuddin.timeshare.data.modals

@kotlinx.serialization.Serializable
data class PostImageVideo (
    val imageOrVideo: Int = 0,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val thumbnail: String = ""
): java.io.Serializable
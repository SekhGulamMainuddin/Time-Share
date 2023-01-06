package com.sekhgmainuddin.timeshare.data.modals

@kotlinx.serialization.Serializable
data class PostImageVideo (
    val imageOrVideo: Int,
    val imageUrl: String?,
    val videoUrl: String?
) {
    constructor() : this (0, null, null)
}
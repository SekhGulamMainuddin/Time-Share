package com.sekhgmainuddin.timeshare.data.modals


@kotlinx.serialization.Serializable
data class Status (
    val statusId: String = "",
    val type: String = "",
    val urlOrText: String = "",
    val statusUploadTime: Long = 0L,
)
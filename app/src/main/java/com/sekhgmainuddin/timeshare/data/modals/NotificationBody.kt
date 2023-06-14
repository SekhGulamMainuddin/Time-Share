package com.sekhgmainuddin.timeshare.data.modals

import kotlinx.serialization.Serializable

@Serializable
data class NotificationBody(
    val title: String,
    val body: String
)
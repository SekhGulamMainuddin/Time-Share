package com.sekhgmainuddin.timeshare.data.modals

import com.google.gson.annotations.SerializedName

data class Notification(
    @SerializedName("to") val token: String = "",
    @SerializedName("notification") val notificationBody: NotificationBody
)
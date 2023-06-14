package com.sekhgmainuddin.timeshare.data.modals

data class CreateGroupNotificationToken(
    val notification_key_name: String,
    val operation: String = "create",
    val registration_ids: List<String>
)
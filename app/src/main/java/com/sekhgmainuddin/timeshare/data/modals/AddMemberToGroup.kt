package com.sekhgmainuddin.timeshare.data.modals

data class AddMemberToGroup(
    val notification_key: String,
    val notification_key_name: String,
    val operation: String = "add",
    val registration_ids: List<String>
)
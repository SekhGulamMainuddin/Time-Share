package com.sekhgmainuddin.timeshare.services

import android.util.Log
import com.sekhgmainuddin.timeshare.data.modals.AddMemberToGroup
import com.sekhgmainuddin.timeshare.data.modals.CreateGroupNotificationToken
import com.sekhgmainuddin.timeshare.data.modals.Notification
import com.sekhgmainuddin.timeshare.data.remote.NotificationSend
import javax.inject.Inject

class FCMNotificationRepository @Inject constructor(
    private val notificationSend: NotificationSend,
) {

    suspend fun sendNotification(notification: Notification) {
        try {
            val response = notificationSend.sendNotification(notification)
            if (response.isSuccessful) {
                Log.d(
                    "sendNotification",
                    "sendNotification: ${notification.token} \n ${response.body()}"
                )
            } else {
                Log.d(
                    "sendNotification",
                    "sendNotificationError: ${response.errorBody()?.string()}"
                )
            }
        } catch (e: Exception) {
            Log.d("sendNotification", "sendNotificationException: $e")
        }
    }

    suspend fun createGroupNotificationToken(groupName: String, groupMemberNotificationTokens: List<String>): String? {
        try {
            val response= notificationSend.createGroupToken(
                CreateGroupNotificationToken(
                    notification_key_name = groupName.trim().replace(" ",""),
                    registration_ids = groupMemberNotificationTokens
                )
            )
            return if (response.isSuccessful){
                response.body()?.notification_key
            }else{
                null
            }
        } catch (e: Exception) {
            Log.d("createGroupNotificationToken", "createGroupNotificationToken: $e")
            return null
        }
    }

    suspend fun addGroupMember(groupName: String, groupNotificationKey: String, groupMemberNotificationTokens: List<String>): String? {
        try {
            val response= notificationSend.addGroupMember(
                AddMemberToGroup(
                    notification_key_name = groupName.trim().replace(" ",""),
                    registration_ids = groupMemberNotificationTokens,
                    notification_key = groupNotificationKey
                )
            )
            return if (response.isSuccessful){
                response.body()?.notification_key
            }else{
                null
            }
        } catch (e: Exception) {
            Log.d("createGroupNotificationToken", "createGroupNotificationToken: $e")
            return null
        }
    }

}
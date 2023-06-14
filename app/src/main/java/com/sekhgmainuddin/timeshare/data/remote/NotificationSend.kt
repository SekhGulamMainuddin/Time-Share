package com.sekhgmainuddin.timeshare.data.remote

import com.sekhgmainuddin.timeshare.data.modals.AddMemberToGroup
import com.sekhgmainuddin.timeshare.data.modals.AuthResponse
import com.sekhgmainuddin.timeshare.data.modals.CreateGroupNotificationToken
import com.sekhgmainuddin.timeshare.data.modals.GroupNotificationKey
import com.sekhgmainuddin.timeshare.data.modals.Notification
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface NotificationSend {

    @POST("fcm/send")
    suspend fun sendNotification(@Body notification: Notification): Response<AuthResponse>

    @POST("fcm/notification")
    suspend fun createGroupToken(@Body createGroupNotificationToken: CreateGroupNotificationToken): Response<GroupNotificationKey>

    @POST("fcm/notification")
    suspend fun addGroupMember(@Body addMemberToGroup: AddMemberToGroup): Response<GroupNotificationKey>

}
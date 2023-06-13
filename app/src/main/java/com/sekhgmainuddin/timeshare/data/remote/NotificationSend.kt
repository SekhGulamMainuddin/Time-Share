package com.sekhgmainuddin.timeshare.data.remote

import com.sekhgmainuddin.timeshare.data.modals.AuthResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationSend {

    @FormUrlEncoded
    @POST("fcm/send")
    suspend fun sendNotification(
        @Field("message") message: String,
        @Field("token") token: String)
            : Response<AuthResponse>

}
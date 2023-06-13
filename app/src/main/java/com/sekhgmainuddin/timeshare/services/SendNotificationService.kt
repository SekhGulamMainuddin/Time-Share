package com.sekhgmainuddin.timeshare.services

import android.util.Log
import com.google.firebase.firestore.SetOptions
import com.sekhgmainuddin.timeshare.data.remote.NotificationSend
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SendNotificationService @Inject constructor(
    private val notificationSend: NotificationSend,
) {

    suspend fun sendNotification(message: String, token: String) {
        try {
            val response = notificationSend.sendNotification(message, token)
            if (response.isSuccessful){
                Log.d("sendNotification", "sendNotification: ${response.body()}")
            }else{
                Log.d("sendNotification", "sendNotificationError: ${response.errorBody()?.string()}")
            }
        }catch (e: Exception){
            Log.d("sendNotification", "sendNotificationException: $e")
        }
    }

}
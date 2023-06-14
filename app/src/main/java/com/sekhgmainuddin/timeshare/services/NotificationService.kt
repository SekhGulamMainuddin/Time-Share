package com.sekhgmainuddin.timeshare.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.ui.home.HomeRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class NotificationService :
    FirebaseMessagingService() {

    @Inject
    lateinit var homeRepository: HomeRepository

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onMessageReceived(message: RemoteMessage) {
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, "CHANNEL_ID")
//        val resultIntent = Intent(this, SplashScreen::class.java)
//        val pendingIntent =
//            PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
//        val body= Json.decodeFromString<NotificationBody>(message.notification?.body.toString())
        val body = Json.decodeFromString<Map<String, String>>(message.notification?.body.toString())
        Log.d("notificationMessage", "onMessageReceived: $body")
        if (body["type"] == "GROUPMESSAGE" && body["by"] == firebaseAuth.currentUser?.uid) {
            return
        }
        builder.setContentTitle(message.notification?.title)
        builder.setContentText("${if (body["type"] == "GROUPMESSAGE" || body["type"] == "CHATMESSAGE") body["profileName"] + ": " + body["message"] else body["message"]}")
        builder.setSmallIcon(R.drawable.time_share_icon)
//        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(true)

        val mNotificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "time_share_notification_channel"
        val channel = NotificationChannel(
            channelId,
            "Time Share Channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        mNotificationManager.createNotificationChannel(channel)
        builder.setChannelId(channelId)
        mNotificationManager.notify(100, builder.build())
    }

    override fun onNewToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            homeRepository.updateToken(token)
        }
    }
}
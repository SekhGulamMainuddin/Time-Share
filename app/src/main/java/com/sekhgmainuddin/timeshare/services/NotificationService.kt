package com.sekhgmainuddin.timeshare.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.ui.home.HomeRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationService:
    FirebaseMessagingService() {

    @Inject
    lateinit var homeRepository: HomeRepository

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("notificationMessage", "onMessageReceived: ${message.data}")
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, "CHANNEL_ID")
//        val resultIntent = Intent(this, SplashScreen::class.java)
//        val pendingIntent =
//            PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentTitle(message.notification?.title)
        builder.setContentText(message.notification?.body)
        builder.setSmallIcon(R.drawable.time_share_icon)
//        builder.setContentIntent(pendingIntent)
//        builder.setStyle(BigTextStyle().bigText(remoteMessage.getNotification().getBody()))
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
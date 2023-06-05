package com.sekhgmainuddin.timeshare.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

class UploadService: Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent!=null && intent.action!=null){

        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
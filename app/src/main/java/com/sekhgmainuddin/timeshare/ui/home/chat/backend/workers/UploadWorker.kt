package com.sekhgmainuddin.timeshare.ui.home.chat.backend.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class UploadWorker(context: Context, params: WorkerParameters): Worker(context, params) {
    override fun doWork(): Result {
        val content: String?= inputData.getString("contentUri")
        val type: Int= inputData.getInt("type",-1)

        return if (content!=null){
            when(type){

            }
            Result.success()
        }else{
            Result.failure()
        }
    }
}
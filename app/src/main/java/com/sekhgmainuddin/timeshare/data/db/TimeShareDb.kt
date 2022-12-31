package com.sekhgmainuddin.timeshare.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sekhgmainuddin.timeshare.data.db.entities.ChatEntity
import com.sekhgmainuddin.timeshare.data.db.entities.RecentProfileChatsEntity

@Database(entities = [ChatEntity::class, RecentProfileChatsEntity::class], version = 6, exportSchema = false)
abstract class TimeShareDb: RoomDatabase() {

    abstract fun getDao(): TimeShareDbDao

}
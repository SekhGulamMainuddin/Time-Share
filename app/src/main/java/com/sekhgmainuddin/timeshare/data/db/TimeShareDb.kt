package com.sekhgmainuddin.timeshare.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sekhgmainuddin.timeshare.data.db.entities.ChatEntity
import com.sekhgmainuddin.timeshare.data.db.entities.PostEntity
import com.sekhgmainuddin.timeshare.data.db.entities.RecentProfileChatsEntity

@Database(entities = [ChatEntity::class, RecentProfileChatsEntity::class, PostEntity::class], version = 8, exportSchema = false)
@TypeConverters(Converter::class)
abstract class TimeShareDb: RoomDatabase() {

    abstract fun getDao(): TimeShareDbDao

}
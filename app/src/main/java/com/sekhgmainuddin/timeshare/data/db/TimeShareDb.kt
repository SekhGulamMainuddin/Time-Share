package com.sekhgmainuddin.timeshare.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sekhgmainuddin.timeshare.data.db.entities.ChatEntity

@Database(entities = [ChatEntity::class], version = 1, exportSchema = false)
abstract class TimeShareDb: RoomDatabase() {

    abstract fun getDao(): TimeShareDbDao

}
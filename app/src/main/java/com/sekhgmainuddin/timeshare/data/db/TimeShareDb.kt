package com.sekhgmainuddin.timeshare.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sekhgmainuddin.timeshare.data.db.entities.ChatEntity
import com.sekhgmainuddin.timeshare.data.db.entities.GroupEntity
import com.sekhgmainuddin.timeshare.data.db.entities.MyStatus
import com.sekhgmainuddin.timeshare.data.db.entities.PostEntity
import com.sekhgmainuddin.timeshare.data.db.entities.RecentProfileChatsEntity
import com.sekhgmainuddin.timeshare.data.db.entities.SavedPostEntity
import com.sekhgmainuddin.timeshare.data.db.entities.UserEntity

@Database(
    entities = [ChatEntity::class, RecentProfileChatsEntity::class, PostEntity::class, UserEntity::class, GroupEntity::class, MyStatus::class, SavedPostEntity::class],
    version = 17,
    exportSchema = false
)
@TypeConverters(Converter::class)
abstract class TimeShareDb : RoomDatabase() {

    abstract fun getDao(): TimeShareDbDao

}
package com.sekhgmainuddin.timeshare.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.sekhgmainuddin.timeshare.data.db.Converter
import com.sekhgmainuddin.timeshare.data.modals.User

@Entity(tableName = "user_table")
data class UserEntity(
    @ColumnInfo(name = "userId") @PrimaryKey(autoGenerate = false) val userId: String,
    @ColumnInfo(name = "user") @TypeConverters(Converter::class) val user: User,
    @ColumnInfo(name = "friends") @TypeConverters(Converter::class) val friends: Map<String, User>,
    @ColumnInfo(name = "following") @TypeConverters(Converter::class) val following: Map<String, User>,
)
package com.sekhgmainuddin.timeshare.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_entity")
data class ChatEntity (
    @ColumnInfo val name: String,
    @PrimaryKey(autoGenerate = true) val id: Int
)
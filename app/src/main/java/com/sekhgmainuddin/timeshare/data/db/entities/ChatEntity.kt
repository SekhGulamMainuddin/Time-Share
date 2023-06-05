package com.sekhgmainuddin.timeshare.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_entity")
data class ChatEntity (
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = false) val id: String,
    @ColumnInfo(name = "chatId") val chatId: String,
    @ColumnInfo(name = "senderId") val senderId: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "document") val document: String?,
    @ColumnInfo(name = "time") val time: Long,
    @ColumnInfo(name = "messageStatus") val messageStatus: String
)
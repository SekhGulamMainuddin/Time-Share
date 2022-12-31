package com.sekhgmainuddin.timeshare.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "chat_entity", primaryKeys = ["chatId","time"])
data class ChatEntity (
    @ColumnInfo(name = "chatId") val chatId: String,
    @ColumnInfo(name = "senderId") val senderId: String,
    @ColumnInfo(name = "type") val type: Int,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "document") val document: String?,
    @ColumnInfo(name = "time") val time: Long,
    @ColumnInfo(name = "messageStatus") val messageStatus: Int
)
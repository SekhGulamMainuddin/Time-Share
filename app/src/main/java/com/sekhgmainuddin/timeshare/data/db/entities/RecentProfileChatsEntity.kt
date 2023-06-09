package com.sekhgmainuddin.timeshare.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.sekhgmainuddin.timeshare.data.db.Converter

@Entity(tableName = "recent_profile_chats_entity")
data class RecentProfileChatsEntity(
    @ColumnInfo(name = "profileId") @PrimaryKey(autoGenerate = false) val profileId: String,
    @ColumnInfo(name = "profileName") val profileName: String,
    @ColumnInfo(name = "profileImageUrl") val profileImageUrl: String,
    @ColumnInfo(name = "recentMessage") val recentMessage: String,
    @ColumnInfo(name = "lastMessageTime") val lastMessageTime: Long,
    @ColumnInfo(name = "numberOfUnseenMessages") val numberOfUnseenMessages: Int,
    @ColumnInfo(name = "typeGroup") var typeGroup: Boolean,
    @ColumnInfo(name = "ifGroupParticipants") @TypeConverters(Converter::class) var ifGroupParticipants: List<String>
)
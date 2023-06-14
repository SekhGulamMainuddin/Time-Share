package com.sekhgmainuddin.timeshare.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.sekhgmainuddin.timeshare.data.db.Converter
import com.sekhgmainuddin.timeshare.data.modals.User
import java.io.Serializable

@kotlinx.serialization.Serializable
@Entity(tableName = "group_entity")
data class GroupEntity(
    @ColumnInfo(name = "groupId") @PrimaryKey(autoGenerate = false) val groupId: String,
    @ColumnInfo(name = "groupName") val groupName: String,
    @ColumnInfo(name = "groupImageUrl") val groupImageUrl: String,
    @ColumnInfo(name = "groupDesc") val groupDesc: String,
    @ColumnInfo(name = "groupMembers") @TypeConverters(Converter::class) val groupMembers: Map<String, User>,
    @ColumnInfo(name = "notificationToken") val notificationToken: String
) : Serializable
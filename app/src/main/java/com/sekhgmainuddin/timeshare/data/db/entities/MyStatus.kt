package com.sekhgmainuddin.timeshare.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.sekhgmainuddin.timeshare.data.db.Converter

@Entity(tableName = "my_status")
data class MyStatus(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = false) val id: String,
    @ColumnInfo(name = "statusId") val statusId: String = "",
    @ColumnInfo(name = "type") val type: String = "",
    @ColumnInfo(name = "urlOrText") val urlOrText: String = "",
    @ColumnInfo(name = "statusUploadTime") val statusUploadTime: Long = 0L,
    @ColumnInfo(name = "statusSeenIds") @TypeConverters(Converter::class) val statusSeenIds: List<String> = arrayListOf()
)
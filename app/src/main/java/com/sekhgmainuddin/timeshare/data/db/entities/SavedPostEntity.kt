package com.sekhgmainuddin.timeshare.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.sekhgmainuddin.timeshare.data.db.Converter
import com.sekhgmainuddin.timeshare.data.modals.Post

@Entity(tableName = "saved_post")
data class SavedPostEntity(
    @ColumnInfo("postId") @PrimaryKey(autoGenerate = false) val postId: String,
    @ColumnInfo("post") @TypeConverters(Converter::class) val post: Post
)
package com.sekhgmainuddin.timeshare.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.sekhgmainuddin.timeshare.data.db.Converter
import com.sekhgmainuddin.timeshare.data.modals.PostImageVideo

@Entity(tableName = "post_entity")
data class PostEntity (
    @ColumnInfo(name = "postId") @PrimaryKey(autoGenerate = false) val postId: String = "",
    @ColumnInfo(name = "creatorId") val creatorId: String = "",
    @ColumnInfo(name = "postDesc") val postDesc: String = "",
    @ColumnInfo(name = "postTime") val postTime: Long = 0,
    @ColumnInfo(name = "postContent") @TypeConverters(Converter::class) val postContent: MutableList<PostImageVideo>? = null,
    @ColumnInfo(name = "creatorName") val creatorName: String = "",
    @ColumnInfo(name = "creatorProfileImage") val creatorProfileImage: String = "",
    @ColumnInfo(name = "likesCount") var likesCount: Int = 0,
    @ColumnInfo(name = "commentsCount") val commentCount: Int = 0,
    @ColumnInfo(name = "likedAndCommentByMe") var likedAndCommentByMe: Int = 0,
    @ColumnInfo(name = "myComment") val myComment: String = ""
)
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
    @ColumnInfo(name = "postId") @PrimaryKey(autoGenerate = false) val postId: String,
    @ColumnInfo(name = "creatorId") val creatorId: String,
    @ColumnInfo(name = "postDesc") val postDesc: String,
    @ColumnInfo(name = "postTime") val postTime: Long,
    @ColumnInfo(name = "postContent") @TypeConverters(Converter::class) val postContent: MutableList<PostImageVideo>?,
    @ColumnInfo(name = "creatorName") val creatorName: String,
    @ColumnInfo(name = "creatorProfileImage") val creatorProfileImage: String,
    @ColumnInfo(name = "likesCount") val likesCount: Int,
    @ColumnInfo(name = "commentsCount") val commentCount: Int,
    @ColumnInfo(name = "likedAndCommentByMe") val likedAndCommentByMe: Int,
    @ColumnInfo(name = "myComment") val myComment: String
)
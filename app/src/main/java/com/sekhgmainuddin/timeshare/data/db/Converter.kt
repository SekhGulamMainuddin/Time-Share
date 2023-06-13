package com.sekhgmainuddin.timeshare.data.db

import androidx.room.TypeConverter
import com.sekhgmainuddin.timeshare.data.modals.Post
import com.sekhgmainuddin.timeshare.data.modals.PostImageVideo
import com.sekhgmainuddin.timeshare.data.modals.User
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
class Converter {
    @TypeConverter
    fun fromList(value : MutableList<PostImageVideo>) = Json.encodeToString(value)

    @TypeConverter
    fun toList(value: String) = Json.decodeFromString<MutableList<PostImageVideo>>(value)

    @TypeConverter
    fun fromUserList(value : Map<String, User>) = Json.encodeToString(value)

    @TypeConverter
    fun toUserList(value: String) = Json.decodeFromString<Map<String, User>>(value)

    @TypeConverter
    fun fromGroupIDList(value : List<String>) = Json.encodeToString(value)

    @TypeConverter
    fun toGroupIDList(value: String) = Json.decodeFromString<List<String>>(value)

    @TypeConverter
    fun fromUser(value : User) = Json.encodeToString(value)

    @TypeConverter
    fun toUser(value: String) = Json.decodeFromString<User>(value)

    @TypeConverter
    fun fromPost(value : Post) = Json.encodeToString(value)

    @TypeConverter
    fun toPost(value: String) = Json.decodeFromString<Post>(value)


}
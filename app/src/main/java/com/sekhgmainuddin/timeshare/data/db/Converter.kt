package com.sekhgmainuddin.timeshare.data.db

import androidx.room.TypeConverter
import com.sekhgmainuddin.timeshare.data.modals.PostImageVideo
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
class Converter {
    @TypeConverter
    fun fromList(value : List<PostImageVideo>) = Json.encodeToString(value)

    @TypeConverter
    fun toList(value: String) = Json.decodeFromString<List<PostImageVideo>>(value)
}
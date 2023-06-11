package com.sekhgmainuddin.timeshare.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sekhgmainuddin.timeshare.data.db.entities.ChatEntity
import com.sekhgmainuddin.timeshare.data.db.entities.GroupEntity
import com.sekhgmainuddin.timeshare.data.db.entities.MyStatus
import com.sekhgmainuddin.timeshare.data.db.entities.PostEntity
import com.sekhgmainuddin.timeshare.data.db.entities.RecentProfileChatsEntity
import com.sekhgmainuddin.timeshare.data.db.entities.UserEntity

@Dao
interface TimeShareDbDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChat(chats: ChatEntity)

    @Delete
    fun deleteChat(chats: ChatEntity)

    @Query("DELETE from chat_entity")
    fun deleteAllChats()

    @Query("SELECT * from chat_entity order by time")
    fun getChats(): LiveData<List<ChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecentProfileChat(recentProfileChats: RecentProfileChatsEntity)

    @Delete
    fun deleteRecentProfileChat(recentProfileChats: RecentProfileChatsEntity)

    @Query("SELECT * from recent_profile_chats_entity order by lastMessageTime DESC")
    fun getRecentChatsList(): LiveData<List<RecentProfileChatsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPost(posts: PostEntity)

    @Query("DELETE from post_entity")
    fun deleteAllPosts()

    @Query("SELECT * from post_entity order by postTime DESC")
    fun getAllPosts(): LiveData<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: UserEntity)

    @Query("SELECT * from user_table")
    fun getUser() : LiveData<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroup(group: GroupEntity)

    @Query("SELECT * from group_entity")
    fun getGroups(): LiveData<List<GroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStatus(status: MyStatus)

    @Query("SELECT * from my_status")
    fun getStatus(): LiveData<List<MyStatus>>


}
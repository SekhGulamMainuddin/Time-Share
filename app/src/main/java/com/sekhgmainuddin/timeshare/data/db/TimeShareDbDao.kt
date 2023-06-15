package com.sekhgmainuddin.timeshare.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sekhgmainuddin.timeshare.data.db.entities.ChatEntity
import com.sekhgmainuddin.timeshare.data.db.entities.GroupEntity
import com.sekhgmainuddin.timeshare.data.db.entities.PostEntity
import com.sekhgmainuddin.timeshare.data.db.entities.RecentProfileChatsEntity
import com.sekhgmainuddin.timeshare.data.db.entities.SavedPostEntity
import com.sekhgmainuddin.timeshare.data.db.entities.UserEntity

@Dao
interface TimeShareDbDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChat(chats: ChatEntity)

    @Delete
    fun deleteChat(chats: ChatEntity)

    @Query("delete from chat_entity")
    fun deleteAllChats()

    @Query("select * from chat_entity order by time")
    fun getChats(): LiveData<List<ChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecentProfileChat(recentProfileChats: RecentProfileChatsEntity)

    @Delete
    fun deleteRecentProfileChat(recentProfileChats: RecentProfileChatsEntity)

    @Query("select * from recent_profile_chats_entity order by lastMessageTime DESC")
    fun getRecentChatsList(): LiveData<List<RecentProfileChatsEntity>>

    @Query("delete from recent_profile_chats_entity")
    fun deleteAllRecentChatProfiles()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPost(posts: PostEntity)

    @Query("delete from post_entity")
    fun deleteAllPosts()

    @Query("select * from post_entity order by postTime DESC")
    fun getAllPosts(): LiveData<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: UserEntity)

    @Query("select * from user_table")
    fun getUser() : LiveData<List<UserEntity>>

    @Query("delete from user_table")
    fun deleteUserData()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroup(group: GroupEntity)

    @Query("select * from group_entity")
    fun getGroups(): LiveData<List<GroupEntity>>

    @Query("delete from group_entity")
    fun deleteAllGroupData()

    @Insert
    fun insertPost(savedPostEntity: SavedPostEntity)

    @Query("delete from saved_post where postId=:id")
    fun deletePost(id: String)

    @Query("select * from saved_post")
    fun getAllSavedPost(): LiveData<List<SavedPostEntity>>

    @Query("delete from saved_post")
    fun deleteAllSavedPosts()

}
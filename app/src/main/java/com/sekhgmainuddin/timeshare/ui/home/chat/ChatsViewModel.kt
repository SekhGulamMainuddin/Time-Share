package com.sekhgmainuddin.timeshare.ui.home.chat

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sekhgmainuddin.timeshare.data.db.TimeShareDb
import com.sekhgmainuddin.timeshare.data.db.TimeShareDbDao
import com.sekhgmainuddin.timeshare.data.db.entities.ChatEntity
import com.sekhgmainuddin.timeshare.data.db.entities.RecentProfileChatsEntity
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.ui.home.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val chatsRepository: ChatsRepository,
    private val dao: TimeShareDb
) :ViewModel(){
    
    val user= chatsRepository.user
    val chatList= chatsRepository.chatsList
    val recentChatProfiles= chatsRepository.recentChatProfiles

    val sendMessageStatus= chatsRepository.messageSent

    fun sendMessage(profileId: String, type: Int, message: String, document: String)= viewModelScope.launch(Dispatchers.IO){
        chatsRepository.sendMessage(profileId, type, message, document)
        chatsRepository.updateRecentMessage(profileId, message, 0)
    }

    @ExperimentalCoroutinesApi
    fun getLatestChats(profileId: String)= viewModelScope.launch(Dispatchers.IO){
        chatsRepository.fetchMessages(profileId).collectLatest {
            when{
                it.isSuccess-> {
                    if (it.getOrNull() != null){
                        for (chat in it.getOrNull()!!){
                            chatsRepository.insertChats(
                                ChatEntity(
                                chat.chatId, chat.senderId, chat.type, chat.message,
                                chat.document, chat.time, chat.messageStatus))
                        }
                    }
                }
                it.isFailure-> {
                    Log.d("chatsList", "onCancelled: ${it.exceptionOrNull()?.printStackTrace()}")
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun getRecentProfileChats()= viewModelScope.launch(Dispatchers.IO){
        chatsRepository.getRecentProfileChats().collectLatest{
            when{
                it.isSuccess-> {
                    if(it.getOrNull() != null){
                        for (recentProfile in it.getOrNull()!!){
                            val userDetail= chatsRepository.getProfileDetails(recentProfile.profileId)
                            chatsRepository.insertRecentChatProfiles(
                                RecentProfileChatsEntity(recentProfile.profileId, userDetail?.name?:"",
                                    userDetail?.imageUrl?:"", recentProfile.recentMessage,
                                    recentProfile.lastMessageTime, recentProfile.numberOfUnseenMessages))
                        }
                    }
                }
                it.isFailure-> {
                    Log.d("recentChatProfiles", "onCancelled: ${it.exceptionOrNull()?.printStackTrace()}")
                }
            }
        }
    }

    val profileDetail= MutableLiveData<User>()

    fun getProfileDetails(profileId: String)= viewModelScope.launch(Dispatchers.IO){
//        chatsRepository.getProfileInfo(profileId).collectLatest {
//            if (it.isSuccess && it.getOrNull() != null){
//                profileDetail.postValue(it.getOrNull())
//            }
//        }
    }




}
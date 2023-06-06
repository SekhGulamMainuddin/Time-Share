package com.sekhgmainuddin.timeshare.ui.home.chat

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sekhgmainuddin.timeshare.data.db.TimeShareDb
import com.sekhgmainuddin.timeshare.data.db.entities.ChatEntity
import com.sekhgmainuddin.timeshare.data.db.entities.RecentProfileChatsEntity
import com.sekhgmainuddin.timeshare.data.modals.Chats
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.data.modals.Call
import com.sekhgmainuddin.timeshare.utils.enums.MessageType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val chatsRepository: ChatsRepository,
    private val dao: TimeShareDb
) : ViewModel() {

    val user = chatsRepository.user
    val chatList = chatsRepository.chatsList
    val recentChatProfiles = chatsRepository.recentChatProfiles

    val sendMessageStatus = chatsRepository.messageSent

    fun sendMessage(profileId: String, type: MessageType, message: String, document: String) =
        viewModelScope.launch(Dispatchers.IO) {
            chatsRepository.sendMessage(profileId, type, message, document)
            chatsRepository.updateRecentMessage(profileId, message, 0)
        }

    fun sendMultipleFileMessage(fileList: List<Uri>, receiverId: String) {
        fileList.forEach {
            sendFileMessage(it, "", null, receiverId)
        }
    }

    fun sendFileMessage(uri: Uri?, gif: String, type: MessageType?, receiverId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            chatsRepository.sendMessageFile(uri, gif, type, receiverId)
        }

    @ExperimentalCoroutinesApi
    fun getLatestChats(profileId: String) = viewModelScope.launch(Dispatchers.IO) {
        chatsRepository.fetchMessages(profileId).collectLatest {
            when {
                it.isSuccess -> {
                    val chatNotRead = ArrayList<Chats>()
                    if (it.getOrNull() != null) {
                        for (chat in it.getOrNull()!!) {
                            chatsRepository.insertChats(
                                ChatEntity(
                                    chat.id, chat.chatId, chat.senderId, chat.type,
                                    chat.message, chat.document, chat.time, chat.messageStatus
                                )
                            )
                            if (chat.messageStatus != "SEEN" && chat.senderId == profileId)
                                chatNotRead.add(chat)
                        }
                    }
                    Log.d("markChatRead", "getLatestChats: ${chatNotRead.size}")
                    if (!chatsRepository.updatingTheMessages) {
                        chatsRepository.markMessagesRead(chatNotRead, profileId)
                    }
                }

                it.isFailure -> {
                    Log.d("chatsList", "onCancelled: ${it.exceptionOrNull()?.printStackTrace()}")
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun getRecentProfileChats() = viewModelScope.launch(Dispatchers.IO) {
        chatsRepository.getRecentProfileChats().collectLatest {
            when {
                it.isSuccess -> {
                    if (it.getOrNull() != null) {
                        for (recentProfile in it.getOrNull()!!) {
                            val userDetail =
                                chatsRepository.getProfileDetails(recentProfile.profileId)
                            chatsRepository.insertRecentChatProfiles(
                                RecentProfileChatsEntity(
                                    recentProfile.profileId,
                                    userDetail?.name ?: "",
                                    userDetail?.imageUrl ?: "",
                                    recentProfile.recentMessage,
                                    recentProfile.lastMessageTime,
                                    recentProfile.numberOfUnseenMessages
                                )
                            )
                        }
                    }
                }

                it.isFailure -> {
                    Log.d(
                        "recentChatProfiles",
                        "onCancelled: ${it.exceptionOrNull()?.printStackTrace()}"
                    )
                }
            }
        }
    }

    val profileDetail = MutableLiveData<User>()

    fun getProfileDetails(profileId: String) = viewModelScope.launch(Dispatchers.IO) {
//        chatsRepository.getProfileInfo(profileId).collectLatest {
//            if (it.isSuccess && it.getOrNull() != null){
//                profileDetail.postValue(it.getOrNull())
//            }
//        }
    }

    fun deleteChat(chatEntity: ChatEntity) = viewModelScope.launch(Dispatchers.IO) {
        chatsRepository.deleteChat(chatEntity)
    }

    val callSuccess = MutableLiveData<Result<Call>>()
    fun makeCall(receiverProfile: User, token: String, uid: Int, typeVideo: Boolean, callId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val result = chatsRepository.makeCall(receiverProfile, token, uid, typeVideo, callId)
            if (result.isSuccess) {
                result.getOrNull()?.let{ callSuccess.postValue(Result.success(it)) }
            } else {
                callSuccess.postValue(
                    Result.failure(
                        result.exceptionOrNull()
                            ?: Exception("Cannot make video call. Some Error Occurred")
                    )
                )
            }
        }

    fun deleteCall(callId: String)= viewModelScope.launch(Dispatchers.IO){
        chatsRepository.deleteCallDetails(callId)
    }

    val callStatus= MutableLiveData<Result<Call>>()
    fun observeCall()= viewModelScope.launch(Dispatchers.IO){
        chatsRepository.observeCall().collectLatest { result->
            if (result.isSuccess){
                result.getOrNull()?.let{ callStatus.postValue(Result.success(it)) }
            } else {
                result.exceptionOrNull()?.let { callStatus.postValue(Result.failure(it)) }
            }
        }
    }

}
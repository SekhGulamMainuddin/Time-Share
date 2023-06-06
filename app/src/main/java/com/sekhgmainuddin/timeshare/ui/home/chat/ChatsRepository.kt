package com.sekhgmainuddin.timeshare.ui.home.chat

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.sekhgmainuddin.timeshare.data.db.TimeShareDb
import com.sekhgmainuddin.timeshare.data.db.entities.ChatEntity
import com.sekhgmainuddin.timeshare.data.db.entities.RecentProfileChatsEntity
import com.sekhgmainuddin.timeshare.data.modals.Chats
import com.sekhgmainuddin.timeshare.data.modals.RecentProfileChats
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.data.modals.VideoCall
import com.sekhgmainuddin.timeshare.ui.home.HomeRepository
import com.sekhgmainuddin.timeshare.utils.NetworkResult
import com.sekhgmainuddin.timeshare.utils.Utils.fileFromContentUri
import com.sekhgmainuddin.timeshare.utils.Utils.getFileExtension
import com.sekhgmainuddin.timeshare.utils.Utils.getFirstPage
import com.sekhgmainuddin.timeshare.utils.Utils.saveAsJPG
import com.sekhgmainuddin.timeshare.utils.enums.MessageStatus
import com.sekhgmainuddin.timeshare.utils.enums.MessageType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class ChatsRepository @Inject constructor(
    private val storageReference: StorageReference,
    private val firestore: FirebaseFirestore,
    private val databaseReference: DatabaseReference,
    firebaseAuth: FirebaseAuth,
    timeShareDb: TimeShareDb,
    private val homeRepository: HomeRepository,
    @ApplicationContext val context: Context
) {
    init {
        CoroutineScope(Dispatchers.IO).launch {
            timeShareDb.getDao().deleteAllChats()
        }
    }

    private val firebaseUser = firebaseAuth.currentUser
    private val timeShareDbDao = timeShareDb.getDao()

    val user = timeShareDbDao.getUser()
    val chatsList = timeShareDbDao.getChats()
    val recentChatProfiles = timeShareDbDao.getRecentChatsList()

    suspend fun insertChats(chat: ChatEntity) {
        timeShareDbDao.insertChat(chat)
    }

    suspend fun insertRecentChatProfiles(recentProfileChats: RecentProfileChatsEntity) {
        timeShareDbDao.insertRecentProfileChat(recentProfileChats)
    }

    @ExperimentalCoroutinesApi
    suspend fun getRecentProfileChats() = callbackFlow<Result<List<RecentProfileChats>>> {
        val recentProfileChatsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recentProfileChatsList = ArrayList<RecentProfileChats>()
                if (snapshot.hasChildren()) {
                    for (i in snapshot.children) {
                        i.getValue(RecentProfileChats::class.java)?.let { profileChat ->
                            recentProfileChatsList.add(profileChat)
                        }
                    }
                }
                this@callbackFlow.trySendBlocking(Result.success(recentProfileChatsList))
            }

            override fun onCancelled(error: DatabaseError) {
                this@callbackFlow.trySendBlocking(Result.failure(error.toException()))
            }
        }

        firebaseUser?.let {
            databaseReference.child("ChatsList").child(it.uid)
                .addValueEventListener(recentProfileChatsListener)
        }

        awaitClose {
            firebaseUser?.let {
                databaseReference.child("ChatsList").child(it.uid)
                    .removeEventListener(recentProfileChatsListener)
            }
        }
    }

    private var _messageSent = MutableLiveData<NetworkResult<Boolean>>()
    val messageSent: LiveData<NetworkResult<Boolean>>
        get() = _messageSent

    suspend fun sendMessage(
        profileId: String,
        type: MessageType,
        message: String,
        document: String
    ) {
        var chat: Chats? = null
        var chatId: String? = null
        val time = System.currentTimeMillis()
        var id = ""
        firebaseUser?.uid?.let { fUid ->
            chatId = getChatId(profileId)
            id = "$fUid$time"
            chat = chatId?.let {
                Chats(
                    id,
                    it,
                    fUid,
                    type.name,
                    message,
                    document,
                    time,
                    MessageStatus.MSG_SENT.name
                )
            }
        }
        try {
            chatId?.let { it ->
                databaseReference.child("Chats").child(it).child(id)
                    .setValue(chat).await()
            }
            _messageSent.postValue(NetworkResult.Success(true, 201))
        } catch (e: Exception) {
            Log.d("sendMessageException", "sendMessage: $e")
            _messageSent.postValue(NetworkResult.Error(message = "${e.message}", statusCode = 500))
        }
    }

    suspend fun updateRecentMessage(
        profileId: String,
        recentMessage: String,
        numberOfUnseenMessage: Int
    ) {
        try {
            firebaseUser?.let {
                databaseReference.child("ChatsList").child(it.uid)
                    .child(profileId).setValue(
                        RecentProfileChats(
                            profileId, recentMessage,
                            System.currentTimeMillis(), numberOfUnseenMessage + 1
                        )
                    ).await()
                databaseReference.child("ChatsList").child(profileId)
                    .child(it.uid).setValue(
                        RecentProfileChats(
                            it.uid, recentMessage,
                            System.currentTimeMillis(), numberOfUnseenMessage + 1
                        )
                    ).await()
            }
        } catch (e: Exception) {
            Log.d("updateRecentException", "updateRecentMessage: $e  ${e.message}")
        }
    }

    var updatingTheMessages = false

    suspend fun markMessagesRead(chatsList: ArrayList<Chats>, profileId: String) {
        updatingTheMessages = true
        try {
            val chatId: String? = getChatId(profileId)
            val update = HashMap<String, Any>()
            for (chat in chatsList) {
                chat.messageStatus = MessageStatus.MSG_SEEN.name
                update[chat.id] = chat
            }
            Log.d("chatMarkRead", "markMessagesRead: $update")
            chatId?.let {
                databaseReference.child("Chats").child(it).updateChildren(update).await()
            }

        } catch (e: Exception) {
            Log.d("chatMarkRead", "markMessagesRead: ${e.message}")
        }
        updatingTheMessages = false
    }

    @ExperimentalCoroutinesApi
    suspend fun fetchMessages(profileId: String) = callbackFlow<Result<List<Chats>>> {
        val chatListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatsList = ArrayList<Chats>()
                if (snapshot.hasChildren()) {
                    for (i in snapshot.children) {
                        i.getValue(Chats::class.java)?.let { chat ->
                            chatsList.add(chat)
                        }
                    }
                    this@callbackFlow.trySendBlocking(Result.success(chatsList))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                this@callbackFlow.trySendBlocking(Result.failure(error.toException()))
            }
        }

        val chatId: String? = getChatId(profileId)
        Log.d("chatsList", "fetchMessages: $chatId")
        chatId?.let { it ->
            databaseReference.child("Chats").child(it)
                .addValueEventListener(chatListener)
        }

        awaitClose {
            chatId?.let { it ->
                databaseReference.child("Chats").child(it)
                    .addValueEventListener(chatListener)
            }
        }

    }

    suspend fun getProfileDetails(profileId: String): User? {
        try {
            val response = firestore.collection("Users").document(profileId).get().await()
            if (response.exists())
                return response.toObject(User::class.java)
        } catch (e: Exception) {
            Log.d("profileDetailException", "getProfileDetails: $e")
        }
        return null
    }


    suspend fun sendMessageFile(
        uri: Uri?,
        gif: String,
        typeofMessage: MessageType?,
        receiverId: String
    ) {
        try {
            val type = typeofMessage ?: (
                    when (getFileExtension(uri!!, context)) {
                        "mp3", "wav", "m4a" -> MessageType.AUDIO
                        "mp4", "mkv", "mov" -> MessageType.VIDEO
                        "png", "jpeg", "jpg", "svg" -> MessageType.IMAGE
                        "pdf" -> MessageType.PDF
                        "docx", "doc" -> MessageType.DOCX
                        else -> {
                            MessageType.DOCUMENT
                        }
                    }
                    )
            val chatId = getChatId(receiverId)
            val currentTime = System.currentTimeMillis()
            val path: String = if (uri == null) gif else "Chats/${chatId}/${
                type.name + currentTime.toString() + "." + getFileExtension(uri, context)
            }"
            val url = if (uri == null) gif else homeRepository.uploadFile(uri, null, path)

            var ifPDFURL: String = ""
            if (type == MessageType.PDF) {
                val cursor =
                    context.contentResolver.query(uri!!, null, null, null)
                val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor?.moveToFirst()
                val tempfile = fileFromContentUri(
                    cursor!!.getString(nameIndex!!),
                    context,
                    uri
                )
                val firstPageImage =
                    tempfile.getFirstPage()?.saveAsJPG("${tempfile.name}FirstPage", context) as Uri
                ifPDFURL = homeRepository.uploadFile(
                    firstPageImage, null, "Chats/${chatId}/${
                        tempfile.name + currentTime.toString()
                    }"
                ).toString()
                cursor.close()
            }
            val chat = Chats(
                id = firebaseUser?.uid!! + System.currentTimeMillis(),
                chatId = chatId!!,
                senderId = firebaseUser.uid,
                type = type.name,
                message = if (type == MessageType.PDF) ifPDFURL else "",
                document = url,
                time = currentTime,
                messageStatus = MessageStatus.MSG_SENT.name
            )
            databaseReference.child("Chats").child(chatId).child("${firebaseUser.uid}$currentTime")
                .setValue(chat).await()
            updateRecentMessage(receiverId, "${type.name} ", 0)
            _messageSent.postValue(NetworkResult.Success(true, 201))
        } catch (e: Exception) {
            Log.d("sendFileMessageException", "sendMessageFile: $e")
            _messageSent.postValue(NetworkResult.Error(message = "${e.message}", statusCode = 500))
        }
    }

    fun getChatId(profileId: String): String? {
        var chatId: String? = null
        firebaseUser?.uid?.let {
            if (it.compareTo(profileId) > 0)
                chatId = it + profileId
            else
                chatId = profileId + it
        }
        return chatId
    }


//    suspend fun getProfileInfo(profileId: String)= callbackFlow<Result<User>>{
//        firestore.collection("Users").document("profileId").addSnapshotListener(object : EventListener<DocumentSnapshot>{
//            override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
//                if(value?.exists() == true) {
//                    value.toObject(User::class.java)?.let{
//                        this@callbackFlow.trySendBlocking(Result.success(it))
//                    }
//                }
//                if (error!=null) {
//                    Log.d("firestoreProfileDetails", "onEvent: $error")
//                    this@callbackFlow.trySendBlocking(Result.failure(error))
//                }
//            }
//        })
//
//
//    }

    private var _friendsList = MutableLiveData<NetworkResult<List<User>>>()
    val friendsList: LiveData<NetworkResult<List<User>>>
        get() = _friendsList

//    suspend fun getFriendsList(){
//        try {
//            firebaseUser?.uid?.let {
//                val userData= firestore.collection("Users").document(it).get().await()
//                if (userData.exists()){
//                    val user= userData.toObject(User::class.java)
//                    val
//                }
//            }
//        }catch (e: Exception){
//            _friendsList.postValue(NetworkResult.Error(e.message))
//        }
//    }

    suspend fun deleteChat(chatEntity: ChatEntity) {
        databaseReference.child("Chats").child(chatEntity.chatId).child(chatEntity.id).removeValue()
            .await().also {
                timeShareDbDao.deleteChat(chatEntity)
            }
    }

    suspend fun makeVideoCall(receiverProfile: User, token: String, uid: Int): Result<String> {
        try {
            val callId = UUID.randomUUID().toString()
            firebaseUser?.uid?.let {
                firestore.collection("Call").document(it).set(
                    VideoCall(
                        callId = callId,
                        token = token,
                        uid = uid,
                        callerProfileId = it,
                        receiverProfileId = receiverProfile.userId,
                        answered = true
                    )
                ).await()
                firestore.collection("Call").document(receiverProfile.userId).set(
                    VideoCall(
                        callId = callId,
                        token = token,
                        uid = uid,
                        callerProfileId = it,
                        receiverProfileId = receiverProfile.userId,
                        answered = false
                    )
                ).await()
            }

            return Result.success(callId)
        } catch (e: Exception) {
            Log.d("makeVideoCall", "makeVideoCall: $e")
            return Result.failure(e)
        }

    }

    suspend fun deleteCallDetails(otherUserId: String) {
        try {
            firebaseUser?.uid?.let { firestore.collection("Call").document(it).delete().await() }
            firestore.collection("Call").document(otherUserId).update(mapOf(Pair("uid", -1)))
                .await()
        } catch (e: Exception) {
            Log.d("deleteVideoCall", "deleteVideoCallDetails: $e")
        }
    }

    suspend fun observeCall() = callbackFlow {
        val callSubscriber = firebaseUser?.uid?.let {
            firestore.collection("Call").document(it).addSnapshotListener { snapshot, error ->
                snapshot?.exists()?.let{
                    snapshot.toObject(VideoCall::class.java)?.let {call->
                        trySend(Result.success(call))
                    }
                }
                if (error!=null){
                    trySend(Result.failure(error))
                }
            }
        }

        awaitClose { callSubscriber?.remove() }
    }

}
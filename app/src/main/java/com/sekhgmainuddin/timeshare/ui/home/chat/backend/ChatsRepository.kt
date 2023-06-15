package com.sekhgmainuddin.timeshare.ui.home.chat.backend

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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.sekhgmainuddin.timeshare.data.db.TimeShareDb
import com.sekhgmainuddin.timeshare.data.db.entities.ChatEntity
import com.sekhgmainuddin.timeshare.data.db.entities.GroupEntity
import com.sekhgmainuddin.timeshare.data.db.entities.RecentProfileChatsEntity
import com.sekhgmainuddin.timeshare.data.modals.Chats
import com.sekhgmainuddin.timeshare.data.modals.RecentProfileChats
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.data.modals.Call
import com.sekhgmainuddin.timeshare.data.modals.Group
import com.sekhgmainuddin.timeshare.data.modals.Notification
import com.sekhgmainuddin.timeshare.data.modals.NotificationBody
import com.sekhgmainuddin.timeshare.data.modals.RecentGroupChats
import com.sekhgmainuddin.timeshare.services.FCMNotificationRepository
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

class ChatsRepository @Inject constructor(
    private val storageReference: StorageReference,
    private val firestore: FirebaseFirestore,
    private val databaseReference: DatabaseReference,
    firebaseAuth: FirebaseAuth,
    timeShareDb: TimeShareDb,
    private val homeRepository: HomeRepository,
    @ApplicationContext val context: Context,
    private val fcmNotificationRepository: FCMNotificationRepository
) {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            timeShareDb.getDao().deleteAllChats()
        }
    }

    private val firebaseUser = firebaseAuth.currentUser
    private val timeShareDbDao = timeShareDb.getDao()

    val groups = timeShareDbDao.getGroups()
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
    suspend fun getRecentProfileChats() =
        callbackFlow<Result<List<Pair<RecentProfileChats?, RecentGroupChats?>>>> {
            val recentProfileChatsListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val recentChatsList =
                        ArrayList<Pair<RecentProfileChats?, RecentGroupChats?>>()
                    if (snapshot.hasChildren()) {
                        for (i in snapshot.children) {
                            if (i.hasChild("group")) {
                                i.getValue(RecentGroupChats::class.java)?.let { groupChat ->
                                    recentChatsList.add(Pair(null, groupChat))
                                }
                            } else {
                                i.getValue(RecentProfileChats::class.java)?.let { profileChat ->
                                    recentChatsList.add(Pair(profileChat, null))
                                }
                            }
                        }
                    }
                    this@callbackFlow.trySendBlocking(Result.success(recentChatsList))
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
        document: String,
        isGroup: Boolean,
        groupMembers: List<String>,
        group: GroupEntity?,
        profile: User?
    ) {
        var chat: Chats? = null
        var chatId: String? = null
        val time = System.currentTimeMillis()
        var id = ""
        firebaseUser?.uid?.let { fUid ->
            chatId = if (isGroup) profileId else getChatId(profileId)
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
            updateRecentMessage(profileId, message, 0, isGroup, groupMembers, group, profile)
            _messageSent.postValue(NetworkResult.Success(true, 201))
        } catch (e: Exception) {
            Log.d("sendMessageException", "sendMessage: $e")
            _messageSent.postValue(NetworkResult.Error(message = "${e.message}", statusCode = 500))
        }
    }

    suspend fun updateRecentMessage(
        profileId: String,
        recentMessage: String,
        numberOfUnseenMessage: Int,
        isGroupMessage: Boolean,
        ifGroupParticipantsList: List<String> = emptyList(),
        group: GroupEntity? = null,
        profile: User? = null
    ) {
        try {
            if (isGroupMessage) {
                firebaseUser?.uid?.let { uid ->
                    ifGroupParticipantsList.forEach {
                        databaseReference.child("ChatsList").child(it)
                            .child(profileId).setValue(
                                RecentGroupChats(
                                    profileId, recentMessage,
                                    System.currentTimeMillis(), numberOfUnseenMessage + 1,
                                    true,
                                    uid
                                )
                            ).await()
                    }
                    fcmNotificationRepository.sendNotification(
                        Notification(
                            token = group!!.notificationToken,
                            notificationBody = NotificationBody(
                                "${group.groupName} Message",
                                Json.encodeToString(
                                    mapOf(
                                        Pair(
                                            "profileImage", group.groupImageUrl
                                        ),
                                        Pair(
                                            "profileId", group.groupId
                                        ),
                                        Pair(
                                            "message", recentMessage
                                        ),
                                        Pair(
                                            "type", "GROUPMESSAGE"
                                        ),
                                        Pair(
                                            "by", uid
                                        ),
                                        Pair(
                                            "profileName", user.value?.get(0)?.user?.name
                                        ),
                                        Pair(
                                            "chatId", group.groupId
                                        ),
                                    )
                                )
                            )
                        )
                    )
                }
            } else {
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
                    profile?.notificationToken?.let { token ->
                        fcmNotificationRepository.sendNotification(
                            Notification(
                                token = token,
                                notificationBody = NotificationBody(
                                    profile.name,
                                    Json.encodeToString(
                                        mapOf(
                                            Pair(
                                                "profileImage", profile.imageUrl
                                            ),
                                            Pair(
                                                "profileId", profile.userId
                                            ),
                                            Pair(
                                                "message", recentMessage
                                            ),
                                            Pair(
                                                "type", "CHATMESSAGE"
                                            ),
                                            Pair(
                                                "profileName", user.value?.get(0)?.user?.name
                                            ),
                                            Pair(
                                                "chatId", getChatId(profile.userId)
                                            ),
                                        )
                                    )
                                )
                            )
                        )
                    }
                }
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
    suspend fun fetchMessages(profileId: String, isGroup: Boolean) =
        callbackFlow<Result<List<Chats>>> {
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

            val chatId: String? = if (isGroup) profileId else getChatId(profileId)
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

    suspend fun getGroupDetails(groupId: String): Group? {
        try {
            val response = firestore.collection("Groups").document(groupId).get().await()
            if (response.exists())
                return response.toObject(Group::class.java)
        } catch (e: Exception) {
            Log.d("profileDetailException", "getProfileDetails: $e")
        }
        return null
    }


    suspend fun sendMessageFile(
        uri: Uri?,
        gif: String,
        typeofMessage: MessageType?,
        receiverId: String,
        isGroupMessage: Boolean,
        groupMembers: List<String>,
        group: GroupEntity?,
        profile: User?
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
            val chatId = if (isGroupMessage) receiverId else getChatId(receiverId)
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
            updateRecentMessage(
                receiverId,
                "Shared a ${type.name} ",
                0,
                isGroupMessage,
                groupMembers,
                group,
                profile
            )
            _messageSent.postValue(NetworkResult.Success(true, 201))
        } catch (e: Exception) {
            Log.d("sendFileMessageException", "sendMessageFile: $e")
            _messageSent.postValue(NetworkResult.Error(message = "${e.message}", statusCode = 500))
        }
    }

    fun getChatId(profileId: String, isGroup: Boolean = false): String? {
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

    suspend fun makeCall(
        receiverProfile: User?,
        token: String,
        uid: Int,
        typeVideo: Boolean,
        callId: String,
        group: GroupEntity?
    ): Result<Call> {
        try {
            var call: Call? = null
            if (receiverProfile != null) {
                val user = firestore.collection("Users").document(firebaseUser?.uid!!).get().await()
                    .toObject(User::class.java)
                call = Call(
                    callId = callId,
                    token = token,
                    uid = uid,
                    callerProfileId = firebaseUser.uid,
                    receiverProfileId = receiverProfile.userId,
                    callerProfileImage = user?.imageUrl ?: "",
                    receiverProfileImage = receiverProfile.imageUrl,
                    callerName = user?.name ?: "",
                    receiverName = receiverProfile.name,
                    answered = true,
                    typeVideo = typeVideo
                )
                firestore.collection("Call").document(firebaseUser.uid).set(
                    call
                ).await()
                call.answered = false
                firestore.collection("Call").document(receiverProfile.userId).set(
                    call
                ).await()
            } else {
                call = Call(
                    callId = callId,
                    token = token,
                    uid = uid,
                    callerProfileId = firebaseUser?.uid!!,
                    receiverProfileId = group?.groupId!!,
                    answered = true,
                    typeVideo = typeVideo,
                    isGroupCall = true
                )

                group.groupMembers.keys.forEach {
                    firestore.collection("Call").document(it).set(
                        Call(
                            callId = callId,
                            token = token,
                            uid = uid,
                            callerProfileId = firebaseUser.uid,
                            receiverProfileId = it,
                            answered = false,
                            typeVideo = typeVideo,
                            isGroupCall = true
                        )
                    ).await()
                }
            }
            return Result.success(call!!)
        } catch (e: Exception) {
            Log.d("makeVideoCall", "makeVideoCall: $e")
            return Result.failure(e)
        }

    }

    private var _deleteCallResult = MutableLiveData<Result<Boolean>>()
    val deleteCallResult: LiveData<Result<Boolean>>
        get() = _deleteCallResult

    suspend fun deleteCallDetails(otherUserId: String) {
        try {
            firebaseUser?.uid?.let { firestore.collection("Call").document(it).delete().await() }
            firestore.collection("Call").document(otherUserId).update(mapOf(Pair("uid", -1)))
                .await()
            _deleteCallResult.postValue(Result.success(true))
        } catch (e: Exception) {
            Log.d("deleteVideoCall", "deleteVideoCallDetails: $e")
            _deleteCallResult.postValue(Result.failure(e))
        }
    }

    suspend fun observeCall() = callbackFlow {
        val callSubscriber = firebaseUser?.uid?.let {
            firestore.collection("Call").document(it).addSnapshotListener { snapshot, error ->
                snapshot?.exists()?.let {
                    snapshot.toObject(Call::class.java)?.let { call ->
                        trySend(Result.success(call))
                    }
                }
                if (error != null) {
                    trySend(Result.failure(error))
                }
            }
        }

        awaitClose { callSubscriber?.remove() }
    }

    private var _createGroupResult = MutableLiveData<Result<Group>>()
    val createGroupResult: LiveData<Result<Group>>
        get() = _createGroupResult

    suspend fun createGroup(
        groupName: String,
        groupDesc: String,
        groupImage: Uri?,
        groupMembers: MutableList<String>
    ) = coroutineScope {
        try {
            firebaseUser?.uid?.let { groupMembers.add(it.trim()) }
            val groupId = UUID.randomUUID().toString()
            val imageUrl =
                if (groupImage == null) "https://cdn-icons-png.flaticon.com/512/6387/6387947.png" else homeRepository.uploadFile(
                    groupImage,
                    null,
                    "Groups/$groupId/groupImage.${getFileExtension(groupImage, context)}"
                )
            val group = Group(
                groupId,
                groupName,
                imageUrl ?: "",
                groupDesc,
                groupMembers
            )

            val groupMembersTokens = groupMembers.map { guid ->
                user.value?.get(0)?.friends?.get(guid)?.notificationToken!!
            }

            val notificationToken = fcmNotificationRepository.createGroupNotificationToken(
                groupName,
                groupMembersTokens
            )

            launch {
                firestore.collection("Groups").document(groupId).set(group).await()
                groupMembers.forEach {
                    firestore.collection("Users").document(it)
                        .update("groups", FieldValue.arrayUnion(groupId)).await()
                }
                updateRecentMessage(
                    groupId, "-1", 0, true, groupMembers, GroupEntity(
                        groupId,
                        groupName,
                        imageUrl ?: "",
                        groupDesc,
                        mapOf(),
                        notificationToken!!
                    ), null
                )
                val userMap = homeRepository.getUserDataById(groupMembers.toSet())
                timeShareDbDao.insertGroup(
                    GroupEntity(
                        groupId,
                        groupName,
                        imageUrl ?: "",
                        groupDesc,
                        userMap,
                        notificationToken
                    )
                )
            }

            _createGroupResult.postValue(Result.success(group))
        } catch (e: Exception) {
            Log.d("createGroup", "createGroupException: $e")
            _createGroupResult.postValue(Result.failure(e))
        }
    }

}
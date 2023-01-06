package com.sekhgmainuddin.timeshare.ui.home.chat

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
import com.sekhgmainuddin.timeshare.utils.NetworkResult
import com.sekhgmainuddin.timeshare.utils.Utils.MSG_SENT
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatsRepository @Inject constructor(
    private val storageReference: StorageReference,
    private val firestore: FirebaseFirestore,
    private val databaseReference: DatabaseReference,
    firebaseAuth: FirebaseAuth,
    timeShareDb: TimeShareDb
) {
    private val firebaseUser = firebaseAuth.currentUser
    private val timeShareDbDao = timeShareDb.getDao()

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

    suspend fun sendMessage(profileId: String, type: Int, message: String, document: String) {
        var chat: Chats? = null
        var chatId: String? = null
        firebaseUser?.uid?.let { fUid->
            chatId = if (fUid.compareTo(profileId) > 0)
                fUid + profileId
            else
                profileId + fUid
            chat = chatId?.let { Chats(it, fUid, type, message, document, System.currentTimeMillis(), MSG_SENT) }
        }
        try {
            chatId?.let { it ->
                databaseReference.child("Chats").child(it)
                    .push().setValue(chat).await()
            }
            _messageSent.postValue(NetworkResult.Success(true, 201))
        } catch (e: Exception) {
            Log.d("sendMessageException", "sendMessage: $e")
            _messageSent.postValue(NetworkResult.Error(message = "${e.message}", statusCode = 500))
        }
    }

    suspend fun updateRecentMessage(profileId: String, recentMessage: String, numberOfUnseenMessage: Int) {
        try {
            firebaseUser?.let {
                databaseReference.child("ChatsList").child(it.uid)
                    .child(profileId).setValue(RecentProfileChats(profileId, recentMessage,
                        System.currentTimeMillis(),numberOfUnseenMessage + 1)).await()
                databaseReference.child("ChatsList").child(profileId)
                    .child(it.uid).setValue(RecentProfileChats(it.uid, recentMessage,
                        System.currentTimeMillis(),numberOfUnseenMessage + 1)).await()
            }
        } catch (e: Exception) {
            Log.d("updateRecentException", "updateRecentMessage: $e  ${e.message}")
        }
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
                            Log.d("chatsList", "onDataChange: ${chat.message}")
                        }
                    }
                    this@callbackFlow.trySendBlocking(Result.success(chatsList))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                this@callbackFlow.trySendBlocking(Result.failure(error.toException()))
            }
        }

        var chatId: String? = null
        firebaseUser?.uid?.let {
            if (it.compareTo(profileId) > 0)
                chatId = it + profileId
            else
                chatId = profileId + it
        }
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

    suspend fun getProfileDetails(profileId: String): User?{
        try {
            val response= firestore.collection("Users").document(profileId).get().await()
            if(response.exists())
                return response.toObject(User::class.java)
        } catch (e: Exception){
            Log.d("profileDetailException", "getProfileDetails: $e")
        }
        return null
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

}
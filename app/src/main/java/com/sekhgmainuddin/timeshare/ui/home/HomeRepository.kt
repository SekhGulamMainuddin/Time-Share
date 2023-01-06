package com.sekhgmainuddin.timeshare.ui.home

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.StorageReference
import com.sekhgmainuddin.timeshare.data.db.TimeShareDb
import com.sekhgmainuddin.timeshare.data.db.entities.PostEntity
import com.sekhgmainuddin.timeshare.data.modals.PostImageVideo
import com.sekhgmainuddin.timeshare.data.modals.PostWithOutCreaterImageName
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.utils.NetworkResult
import com.sekhgmainuddin.timeshare.utils.Utils
import com.sekhgmainuddin.timeshare.utils.Utils.isImageOrVideo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseStorage: StorageReference,
    timeShareDb: TimeShareDb,
    @ApplicationContext val context: Context
) {

    private val firebaseUser = firebaseAuth.currentUser
    private val timeShareDbDao = timeShareDb.getDao()


    val allPosts= timeShareDbDao.getAllPosts()

    suspend fun insertPost(post: PostEntity) {
        timeShareDbDao.insertPost(post)
    }

    suspend fun deleteAllPosts() {
        timeShareDbDao.deleteAllPosts()
    }

    suspend fun getUserDataById(listIds: Set<String>) : HashMap<String,User> {
        val usersData= HashMap<String,User>()
        for (i in listIds) {
            val result= firebaseFirestore.collection("Users").document(i).get().await()
            if (result.exists()){
                result.toObject(User::class.java)?.let {
                    usersData[i]= it
                }
            }
        }
        return usersData
    }

    suspend fun getUserData() = callbackFlow<Result<User>> {
        val subscriptionUser = firebaseUser?.uid?.let {
            firebaseFirestore.collection("Users").document(it)
                .addSnapshotListener { snapshot, error ->
                    snapshot?.exists()?.let {
                        snapshot.toObject(User::class.java)
                            ?.let { it1 -> trySend(Result.success(it1)) }
                    }
                    if (error != null)
                        trySend(Result.failure(error))
                }
        }
        awaitClose { subscriptionUser?.remove() }
    }

    private var _addPostStatus = MutableLiveData<NetworkResult<Boolean>>()
    val addPostStatus: LiveData<NetworkResult<Boolean>>
        get() = _addPostStatus

    suspend fun addPost(list: List<Uri>, description: String) {
        try {
            val imageVideoURLList= ArrayList<PostImageVideo>()
            val postLocation= firebaseAuth.uid + System.currentTimeMillis()
            for(i in list.indices){
                val fileExtension= Utils.getFileExtension(list[i], context)
                val response= firebaseStorage.child(
                    "Posts/${firebaseAuth.uid}/$postLocation/" + "item$i." + fileExtension
                ).putFile(list[i]).await()
                val uriTask = response?.storage?.downloadUrl
                while (!uriTask?.isSuccessful!!) {}
                val imageOrVideo= fileExtension?.isImageOrVideo()
                imageOrVideo?.let {
                    PostImageVideo(
                        it,
                        if (it==0) uriTask.result.toString() else "",
                        if (it==1) uriTask.result.toString() else ""
                    )
                }?.let { imageVideoURLList.add(it) }
            }
            val postData= HashMap<String, Any>()
            postData["postId"]= postLocation
            firebaseUser?.uid?.let { postData["creatorId"]= it }
            postData["postDesc"]= description
            postData["postContent"]= imageVideoURLList
            postData["likesCount"]= 0
            postData["commentsCount"]= 0
            postData["likedAndCommentByMe"]= 0
            firebaseFirestore.collection("Posts").document(postLocation).set(postData).await()
            _addPostStatus.postValue(NetworkResult.Success(true,200))
        }
        catch (e: Exception){
            Log.d("addPostException", "addPost: $e")
            _addPostStatus.postValue(NetworkResult.Error(e.stackTraceToString(), statusCode = 500))
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun getLatestPosts(friendList: List<String>) = callbackFlow<Result<Pair<List<PostWithOutCreaterImageName>,Set<String>>>> {
        val postListener= firebaseFirestore.collection("Posts")
            .orderBy("postTime", Query.Direction.DESCENDING).whereIn("creatorId",friendList)
            .limit(10).addSnapshotListener { snapshot, error ->
                val postList= ArrayList<PostWithOutCreaterImageName>()
                val userIds= ArrayList<String>()
                if(error!=null)
                    trySend(Result.failure(error))
                if (snapshot != null) {
                    if (!snapshot.isEmpty){
                        snapshot.documents.forEach {
                            it.toObject(PostWithOutCreaterImageName::class.java)?.let { post ->
                                postList.add(post)
                                userIds.add(post.creatorId)
                            }
                        }
                        trySend(Result.success(Pair(postList, userIds.toSet())))
                    }
                }
            }

        awaitClose { postListener.remove() }

    }

}














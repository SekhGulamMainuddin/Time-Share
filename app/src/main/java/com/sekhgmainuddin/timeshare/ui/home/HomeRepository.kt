package com.sekhgmainuddin.timeshare.ui.home

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.StorageReference
import com.sekhgmainuddin.timeshare.data.db.TimeShareDb
import com.sekhgmainuddin.timeshare.data.db.entities.PostEntity
import com.sekhgmainuddin.timeshare.data.db.entities.UserEntity
import com.sekhgmainuddin.timeshare.data.modals.*
import com.sekhgmainuddin.timeshare.utils.NetworkResult
import com.sekhgmainuddin.timeshare.utils.Utils
import com.sekhgmainuddin.timeshare.utils.Utils.isImageOrVideo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import kotlin.math.log

class HomeRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFireStore: FirebaseFirestore,
    private val firebaseStorage: StorageReference,
    timeShareDb: TimeShareDb,
    @ApplicationContext val context: Context
) {

    private val firebaseUser = firebaseAuth.currentUser
    private val timeShareDbDao = timeShareDb.getDao()


    val allPosts = timeShareDbDao.getAllPosts()

    fun insertPost(post: PostEntity) {
        timeShareDbDao.insertPost(post)
    }

    fun deleteAllPosts() {
        timeShareDbDao.deleteAllPosts()
    }

    fun insert(user: UserEntity) {
        timeShareDbDao.insert(user)
    }

    suspend fun getUserDataById(listIds: Set<String>): HashMap<String, User> {
        val usersData = HashMap<String, User>()
        try {
            for (i in listIds) {
                val result = firebaseFireStore.collection("Users").document(i).get().await()
                if (result.exists()) {
                    result.toObject(User::class.java)?.let {
                        usersData[i] = it
                    }
                }
            }
        } catch (_: Exception) {

        }
        return usersData
    }

    private var _userDetails = MutableLiveData<Result<UserWithFriendFollowerAndFollowingLists>>()
    val userDetails: LiveData<Result<UserWithFriendFollowerAndFollowingLists>>
        get() = _userDetails

    private var _searchUserDetails =
        MutableLiveData<Result<UserWithFriendFollowerAndFollowingLists?>>()
    val searchUserDetails: LiveData<Result<UserWithFriendFollowerAndFollowingLists?>>
        get() = _searchUserDetails

    private var currentLoggedUser: UserWithFriendFollowerAndFollowingLists? = null

    suspend fun getUserData(user_Id: String? = null) {
        try {
            if (user_Id != null)
                _searchUserDetails.postValue(Result.success(null))
            val response = (user_Id ?: firebaseUser?.uid)?.let {
                firebaseFireStore.collection("Users").document(
                    it
                ).get().await()
            }
            Log.d("userSearchCheck", "getUserData: $response")
            if (response?.exists() == true) {
                val otherUserList = ArrayList<String>()
                val user = response.toObject(User::class.java)
                user?.apply {
                    friends?.let { otherUserList.addAll(it) }
                    followers?.let { otherUserList.addAll(it) }
                    following?.let { otherUserList.addAll(it) }
                    getUserDataById(otherUserList.toSet()).let { otherUsers ->
                        val friendList = HashMap<String, User>()
                        val followersList = HashMap<String, User>()
                        val followingList = HashMap<String, User>()
                        friends?.forEach { id ->
                            otherUsers[id]?.let { user ->
                                friendList[id] = user
                            }
                        }
                        followers?.forEach { id ->
                            otherUsers[id]?.let { user ->
                                followersList[id] = user
                            }
                        }
                        following?.forEach { id ->
                            otherUsers[id]?.let { user ->
                                followingList[id] = user
                            }
                        }
                        val userDetails = UserWithFriendFollowerAndFollowingLists(
                            name,
                            this.userId,
                            email,
                            phone,
                            imageUrl,
                            bio,
                            interests,
                            location,
                            activeStatus,
                            friendList,
                            followersList,
                            followingList
                        )
                        if (user_Id == null) {
                            currentLoggedUser = userDetails
                            insert(UserEntity(userDetails.userId, userDetails.friends))
                            Log.d(
                                "userDetails",
                                "getUserData: $followersList $followingList $friendList"
                            )
                            _userDetails.postValue(Result.success(userDetails))
                        } else {
                            Log.d(
                                "userDetails",
                                "getSearchUserData: $followersList $followingList $friendList"
                            )
                            _searchUserDetails.postValue(Result.success(userDetails))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("userDetails", "getUserData: $e")
            if (user_Id == null)
                _userDetails.postValue(Result.failure(e))
            else
                _searchUserDetails.postValue(Result.failure(e))
        }
    }

    var userProfileDetail: User? = null
    suspend fun getUserData() = callbackFlow {
        val subscriptionUser = firebaseUser?.uid?.let {
            firebaseFireStore.collection("Users").document(it)
                .addSnapshotListener { snapshot, error ->
                    snapshot?.exists()?.let {
                        snapshot.toObject(User::class.java)
                            ?.let { it1 ->
                                trySend(Result.success(it1))
                                userProfileDetail = it1
                            }
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
            val imageVideoURLList = ArrayList<PostImageVideo>()
            val postLocation = firebaseAuth.uid + System.currentTimeMillis()
            for (i in list.indices) {
                val fileExtension = Utils.getFileExtension(list[i], context)
                val response = firebaseStorage.child(
                    "Posts/${firebaseAuth.uid}/$postLocation/" + "item$i." + fileExtension
                ).putFile(list[i]).await()
                val uriTask = response?.storage?.downloadUrl
                while (!uriTask?.isSuccessful!!) {
                }
                val imageOrVideo = fileExtension?.isImageOrVideo()
                imageOrVideo?.let {
                    PostImageVideo(
                        it,
                        if (it == 0) uriTask.result.toString() else "",
                        if (it == 1) uriTask.result.toString() else ""
                    )
                }?.let { imageVideoURLList.add(it) }
            }
            val postData = HashMap<String, Any>()
            postData["postId"] = postLocation
            firebaseUser?.uid?.let { postData["creatorId"] = it }
            postData["postDesc"] = description
            postData["postContent"] = imageVideoURLList
            postData["likesCount"] = 0
            postData["commentsCount"] = 0
            postData["likedAndCommentByMe"] = 0
            firebaseFireStore.collection("Posts").document(postLocation).set(postData).await()
            _addPostStatus.postValue(NetworkResult.Success(true, 200))
        } catch (e: Exception) {
            Log.d("addPostException", "addPost: $e")
            _addPostStatus.postValue(NetworkResult.Error(e.stackTraceToString(), statusCode = 500))
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun getLatestPosts(friendList: List<String>) =
        callbackFlow<Result<Pair<List<PostWithOutCreaterImageName>, Set<String>>>> {
            val postListener = firebaseFireStore.collection("Posts")
                .orderBy("postTime", Query.Direction.DESCENDING).whereIn("creatorId", friendList)
                .limit(10).addSnapshotListener { snapshot, error ->
                    val postList = ArrayList<PostWithOutCreaterImageName>()
                    val userIds = ArrayList<String>()
                    if (error != null)
                        trySend(Result.failure(error))
                    if (snapshot != null) {
                        if (!snapshot.isEmpty) {
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

    @ExperimentalCoroutinesApi
    suspend fun getPost(postId: String) = callbackFlow {
        val postListener = firebaseFireStore.collection("Posts").document(postId)
            .addSnapshotListener { snapshot, error ->
                val userList = ArrayList<String>()
                if (snapshot?.exists() == true) {
                    userList.clear()
                    snapshot.toObject(Post::class.java)?.let {
                        it.likeAndComment?.keys?.let { keys -> userList.addAll(keys) }
                        trySend(Result.success(Pair(it, userList.toSet())))
                    }
                }
                if (error != null)
                    trySend(Result.failure(error))
            }

        awaitClose { postListener.remove() }
    }

    suspend fun incrementLike(postId: String) {
        try {
            val mainMap = hashMapOf(
                Pair(
                    "likeAndComment",
                    hashMapOf(Pair(firebaseUser?.uid, hashMapOf(Pair("liked", true))))
                )
            )
            firebaseFireStore.collection("Posts").document(postId)
                .update("likeCount", FieldValue.increment(1)).await()
            firebaseFireStore.collection("Posts").document(postId).set(mainMap, SetOptions.merge())
                .await()
        } catch (_: Exception) {

        }
    }

    suspend fun addComment(postId: String, comment: String) {
        val mainMap = hashMapOf(
            Pair(
                "likeAndComment",
                hashMapOf(Pair(firebaseUser?.uid, hashMapOf(Pair("comment", comment))))
            )
        )
        try {
            firebaseFireStore.collection("Posts").document(postId)
                .update("commentCount", FieldValue.increment(1)).await()
            firebaseFireStore.collection("Posts").document(postId).set(mainMap, SetOptions.merge())
                .await()
        } catch (_: Exception) {

        }
    }


    suspend fun getReels(
        oldReels: List<String>,
        showUserReels: Boolean
    ): Pair<ArrayList<Reel>, Set<String>>? {
        try {
            val reelsResponse = if (showUserReels) {
                firebaseFireStore.collection("Reels").whereEqualTo("creatorId", firebaseUser?.uid)
                    .whereNotIn("reelId", oldReels).orderBy("reelId")
                    .orderBy("reelPostTime", Query.Direction.DESCENDING).limit(10).get().await()
            } else {
                firebaseFireStore.collection("Reels")
                    .whereNotIn("reelId", oldReels).orderBy("reelId")
                    .orderBy("reelPostTime", Query.Direction.DESCENDING)
                    .limit(10).get().await()
            }
            if (!reelsResponse.isEmpty) {
                val userList = ArrayList<String>()
                val reelNewList = ArrayList<Reel>()
                reelsResponse.documents.forEach {
                    it.toObject(Reel::class.java)?.let { reel ->
                        reelNewList.add(reel)
                        userList.add(reel.creatorId)
                        Log.d("newReels", "getReels: $it")
                    }
                }
                if (reelNewList.isNotEmpty())
                    return Pair(reelNewList, userList.toSet())
            }
            return null
        } catch (_: Exception) {
            return null
        }
    }

    private var _userUploadedReels = MutableLiveData<NetworkResult<List<Reel>>>()
    val userUploadedReels: LiveData<NetworkResult<List<Reel>>>
        get() = _userUploadedReels

    private var _searchUserUploadedReels = MutableLiveData<NetworkResult<List<Reel>?>>()
    val searchUserUploadedReels: LiveData<NetworkResult<List<Reel>?>>
        get() = _searchUserUploadedReels

    suspend fun getUserPostedReels(oldReels: List<String>, userId: String?) {
        try {
            if (userId == null)
                _searchUserUploadedReels.postValue(NetworkResult.Success(null, statusCode = 200))
            firebaseUser?.uid.let { id ->
                val reelsResponse =
                    firebaseFireStore.collection("Reels").whereEqualTo("creatorId", userId ?: id)
                        .whereNotIn("reelId", oldReels).orderBy("reelId")
                        .orderBy("reelPostTime", Query.Direction.DESCENDING).limit(20).get().await()
                if (!reelsResponse.isEmpty) {
                    val reelNewList = ArrayList<Reel>()
                    reelsResponse.documents.forEach {
                        it.toObject(Reel::class.java)?.let { reel ->
                            reelNewList.add(reel)
                        }
                    }
                    Log.d("userReelsList", "getUserPostedReels: $reelNewList")
                    if (reelNewList.isNotEmpty()) {
                        if (userId == null)
                            _userUploadedReels.postValue(NetworkResult.Success(reelNewList, 200))
                        else
                            _searchUserUploadedReels.postValue(
                                NetworkResult.Success(
                                    reelNewList,
                                    200
                                )
                            )
                    }
                }
            }
        } catch (_: Exception) {

        }
    }

    suspend fun getCommentsAndLikesByReelsId(reelId: String) = callbackFlow {
        val reelListener = firebaseFireStore.collection("Reels").document(reelId)
            .addSnapshotListener { value, error ->
                if (error != null)
                    trySend(Result.failure(error))
                if (value?.exists() == true) {
                    trySend(Result.success(value.toObject(Reel::class.java)))
                }
            }
        awaitClose { reelListener.remove() }
    }

    private var _likeReelStatus = MutableLiveData<NetworkResult<Boolean>>()
    val likeReelStatus: LiveData<NetworkResult<Boolean>>
        get() = _likeReelStatus

    suspend fun likeReel(reelId: String, liked: Boolean) {
        try {
            val mainMap = hashMapOf(
                Pair(
                    "likeAndComment",
                    hashMapOf(Pair(firebaseUser?.uid, hashMapOf(Pair("liked", !liked))))
                )
            )
            firebaseFireStore.collection("Reels").document(reelId)
                .update(
                    "likeCount",
                    if (!liked) FieldValue.increment(1) else FieldValue.increment(-1)
                ).await()
            firebaseFireStore.collection("Reels").document(reelId).set(mainMap, SetOptions.merge())
                .await()
            _likeReelStatus.postValue(NetworkResult.Success(true, 200))
        } catch (e: Exception) {
            _likeReelStatus.postValue(NetworkResult.Error("Some Error Occurred"))
        }
    }

    private var _commentReelStatus = MutableLiveData<NetworkResult<Boolean>>()
    val commentReelStatus: LiveData<NetworkResult<Boolean>>
        get() = _commentReelStatus

    suspend fun addCommentToReel(reelId: String, comment: String) = coroutineScope {
        try {
            val mainMap = hashMapOf(
                Pair(
                    "likeAndComment",
                    hashMapOf(Pair(firebaseUser?.uid, hashMapOf(Pair("comment", comment))))
                )
            )
            awaitAll(async {
                firebaseFireStore.collection("Reels").document(reelId)
                    .update("commentCount", FieldValue.increment(1))
            }, async {
                firebaseFireStore.collection("Reels").document(reelId)
                    .set(mainMap, SetOptions.merge())
            })

            _commentReelStatus.postValue(NetworkResult.Success(true, 200))
        } catch (e: Exception) {
            _commentReelStatus.postValue(NetworkResult.Error("Some Error Occurred"))
        }
    }

    suspend fun getAllPosts(
        oldList: List<String>,
        forSearchFragment: Boolean
    ): List<Pair<Post, String>>? {
        try {
            val posts = ArrayList<Pair<Post, String>>()
            val postsResponse = firebaseFireStore.collection("Posts")
                .whereNotIn("postId", oldList).orderBy("postId")
                .orderBy("postTime", Query.Direction.DESCENDING).limit(10).get().await()
            if (!postsResponse.isEmpty) {
                postsResponse.documents.forEach {
                    it.toObject(Post::class.java)?.let { post ->
                        var image = ""
                        post.postContent?.forEach { content ->
                            if (content.imageOrVideo == 0)
                                image = content.imageUrl.toString()
                        }
                        posts.add(Pair(post, image))
                    }
                }
                Log.d("AllPostsOfUser", "getAllPosts: $posts")
                return posts
            } else {
                Log.d("AllPostsOfUser", "getAllPosts: Empty")
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun getAllPosts(oldList: List<String>, userId: String?): List<Pair<Post, String>>? {
        try {
            val posts = ArrayList<Pair<Post, String>>()
            val postsResponse = firebaseFireStore.collection("Posts")
                .whereEqualTo("creatorId", userId ?: firebaseUser?.uid)
                .whereNotIn("postId", oldList).orderBy("postId")
                .orderBy("postTime", Query.Direction.DESCENDING).limit(10).get().await()
            if (!postsResponse.isEmpty) {
                postsResponse.documents.forEach {
                    it.toObject(Post::class.java)?.let { post ->
                        var image = ""
                        post.postContent?.forEach { content ->
                            if (content.imageOrVideo == 0)
                                image = content.imageUrl.toString()
                        }
                        posts.add(Pair(post, image))
                    }
                }
                Log.d("AllPostsOfUser", "getAllPosts: $posts")
                return posts
            } else {
                Log.d("AllPostsOfUser", "getAllPosts: Empty")
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    private var _profilesFromSearchQuery =
        MutableLiveData<NetworkResult<ArrayList<Pair<User, String?>>>>()
    val profilesFromSearchQuery: LiveData<NetworkResult<ArrayList<Pair<User, String?>>>>
        get() = _profilesFromSearchQuery

    val searchMap = HashMap<String, ArrayList<Pair<User, String?>>>()

    suspend fun getProfileFromSearch(query: String) {
        try {
            _profilesFromSearchQuery.postValue(NetworkResult.Success(arrayListOf(), 200))
            searchMap[query]?.let {
                _profilesFromSearchQuery.postValue(
                    NetworkResult.Success(
                        it,
                        statusCode = 200
                    )
                )
            }
            val response = firebaseFireStore.collection("Users")
                .whereGreaterThanOrEqualTo("name", query)
                .get().await()

            if (!response.isEmpty) {
                val profiles = ArrayList<Pair<User, String?>>()
                response.documents.forEach {
                    it.toObject(User::class.java)?.let { user ->
                        var mutualFollowers = "Followed by "
                        var firstMutualFollower = true
                        user.followers?.forEach { following ->
                            if (currentLoggedUser?.following?.get(following) != null) {
                                if (firstMutualFollower) {
                                    mutualFollowers += currentLoggedUser?.following?.get(following)?.name
                                    firstMutualFollower = false
                                } else {
                                    mutualFollowers += ", ${
                                        currentLoggedUser?.following?.get(
                                            following
                                        )?.name
                                    }"
                                }
                            }
                        }
                        profiles.add(
                            Pair(
                                user,
                                if (mutualFollowers.length > 12) mutualFollowers else null
                            )
                        )
                    }
                    if (profiles.isNotEmpty()) {
                        _profilesFromSearchQuery.postValue(
                            NetworkResult.Success(
                                profiles,
                                statusCode = 200
                            )
                        )
                        searchMap[query] = profiles
                    } else {
                        _profilesFromSearchQuery.postValue(
                            NetworkResult.Error(
                                null,
                                statusCode = 404
                            )
                        )
                    }
                }
            } else {
                _profilesFromSearchQuery.postValue(NetworkResult.Error(null, statusCode = 4001))
            }
        } catch (e: java.lang.IndexOutOfBoundsException) {
            _profilesFromSearchQuery.postValue(NetworkResult.Error(null, statusCode = 500))
            Log.d("searchQueryException", "getProfileFromSearch: $e")
        }

    }

    suspend fun uploadFile(uri: Uri?, file: File?, path: String): String? {
        return try {
            firebaseStorage.child(path).putFile(uri ?: Uri.fromFile(file))
                .await().storage.downloadUrl.await().toString()
        } catch (e: Exception) {
            null;
        }
    }

    suspend fun checkVideoCall() = callbackFlow {
        val videoCheck = firebaseUser?.uid?.let {
            firebaseFireStore.collection("Call").document(it)
                .addSnapshotListener { snapshot, error ->
                    snapshot?.exists()?.let {
                        snapshot.toObject(VideoCall::class.java)
                            ?.let { it1 ->
                                trySend(Result.success(it1))
                            }
                    }
                    if (error != null)
                        trySend(Result.failure(error))
                }
        }
        awaitClose {
            videoCheck?.remove()
        }
    }

    suspend fun changeCallStatus() {
        try {
            firebaseUser?.uid?.let {
                firebaseFireStore.collection("Call").document(it)
                    .update(mapOf(Pair("answered", true))).await()
            }
        } catch (e: Exception) {
            Log.d("changeVideoCallStatus", "changeVideoCallStatus: $e")
        }
    }

}














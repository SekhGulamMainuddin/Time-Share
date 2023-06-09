package com.sekhgmainuddin.timeshare.ui.home

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.sekhgmainuddin.timeshare.data.db.entities.PostEntity
import com.sekhgmainuddin.timeshare.data.modals.*
import com.sekhgmainuddin.timeshare.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    firebaseAuth: FirebaseAuth
) : ViewModel() {

    val firebaseUser = firebaseAuth.currentUser
    val userData = MutableLiveData<User>()
    val postPassedToView = MutableLiveData<Post>()
    val reelPassedToView = MutableLiveData<Reel>()


    fun getUserData() = viewModelScope.launch(Dispatchers.IO) {
        homeRepository.getUserData().collectLatest { result ->
            if (result.isSuccess) {
                result.getOrNull()?.let {
                    userData.postValue(it)
                    if (it.groups.isNotEmpty()){
                        homeRepository.getGroupDetails(it.groups)
                    }
                }
                Log.d("AllPostsOfUser", "getUserData: ${result.getOrNull()}")
            }
        }
    }

    val userDetails: LiveData<Result<UserWithFriendFollowerAndFollowingLists>>
        get() = homeRepository.userDetails
    val searchUserDetails: LiveData<Result<UserWithFriendFollowerAndFollowingLists?>>
        get() = homeRepository.searchUserDetails

    fun getUserData(userId: String?) = viewModelScope.launch(Dispatchers.IO) {
        homeRepository.getUserData(userId)
    }

    val addPostStatus = homeRepository.addPostStatus

    fun addPost(list: List<Uri>, description: String) = viewModelScope.launch(Dispatchers.IO) {
        homeRepository.addPost(list, description)
    }

    val allPosts = homeRepository.allPosts

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getLatestPosts(friendsList: MutableList<String>) = viewModelScope.launch(Dispatchers.IO) {
        friendsList.add(firebaseUser?.uid!!)
        homeRepository.getLatestPosts(friendsList).collectLatest {
            if (it.isSuccess) {
                it.getOrNull()?.let { data ->
                    homeRepository.deleteAllPosts()
                    val userData = homeRepository.getUserDataById(data.second)
                    for (i in data.first) {
                        val temp = firebaseUser.let { it1 -> i.likeAndComment?.get(it1.uid) }
                        var likeCommentType = 0
                        var comment = ""
                        if (temp != null) {
                            if (temp.comment.isNotEmpty())
                                comment = temp.comment
                            likeCommentType = if (temp.liked && temp.comment.isNotEmpty())
                                3
                            else if (temp.liked)
                                1
                            else if (temp.comment.isNotEmpty())
                                2
                            else
                                0
                        }
                        val user = userData[i.creatorId]
                        val post = PostEntity(
                            i.postId, i.creatorId, i.postDesc,
                            i.postTime, i.postContent, user?.name ?: "",
                            user?.imageUrl ?: "", i.likeCount,
                            i.commentCount, likeCommentType, comment
                        )
                        homeRepository.insertPost(post)
                    }
                }
            }
            if (it.isFailure) {
                Log.d("latestPosts", "getLatestPosts: failure occurred ${it.exceptionOrNull()}")
            }
        }
    }

    val postDetails =
        MutableLiveData<Triple<Post, List<LikeWithProfile>, List<CommentWithProfile>>>()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPost(postId: String) = viewModelScope.launch(Dispatchers.IO) {
        homeRepository.getPost(postId).collectLatest {
            if (it.isSuccess) {
                it.getOrNull()?.let { pair ->
                    var userListResponse = HashMap<String, User>()
                    pair.second.let { userList ->
                        val set = mutableSetOf<String>()
                        set.addAll(userList)
                        set.add(pair.first.creatorId)
                        userListResponse = homeRepository.getUserDataById(set)
                    }
                    val likeList = mutableListOf<LikeWithProfile>()
                    val commentList = mutableListOf<CommentWithProfile>()
                    pair.first.likeAndComment?.forEach { map ->
                        val user = userListResponse[map.key]
                        if (map.value.liked)
                            likeList.add(
                                LikeWithProfile(
                                    map.key,
                                    user?.name ?: " ",
                                    user?.imageUrl ?: " "
                                )
                            )
                        if (map.key != firebaseUser?.uid) {
                            if (map.value.comment.isNotEmpty())
                                commentList.add(
                                    CommentWithProfile(
                                        map.key,
                                        user?.name ?: " ",
                                        user?.imageUrl ?: " ",
                                        map.value.comment
                                    )
                                )
                        }
                    }
                    pair.first.let { post ->
                        val creator = userListResponse[post.creatorId]
                        post.likeAndComment = null
                        post.creatorName = creator?.name ?: ""
                        post.creatorProfileImage = creator?.imageUrl ?: ""
                        postDetails.postValue(Triple(post, likeList, commentList))
                    }
                }
            }
        }
    }

    fun addLike(postId: String) = viewModelScope.launch(Dispatchers.IO) {
        homeRepository.incrementLike(postId)
    }

    fun addComment(postId: String, comment: String) = viewModelScope.launch(Dispatchers.IO) {
        homeRepository.addComment(postId, comment)
    }

    val newReels = MutableLiveData<ArrayList<Reel>>()

    fun getReels(oldReels: List<String> = listOf("noIdPassed"), showUserReels: Boolean = false) =
        viewModelScope.launch(Dispatchers.IO) {
            val result = homeRepository.getReels(oldReels, showUserReels)
            if (result != null) {
                val userList = result.second.let { homeRepository.getUserDataById(it) }
                result.first.forEach { reel ->
                    reel.creatorName = userList[reel.creatorId]?.name ?: ""
                    reel.creatorImageUrl = userList[reel.creatorId]?.imageUrl ?: ""
                    val likeComment = reel.likeAndComment?.get(firebaseUser?.uid)
                    if (likeComment != null) {
                        if (likeComment.liked && likeComment.comment.isNotEmpty())
                            reel.likedAndCommentByMe = 3
                        else if (likeComment.liked)
                            reel.likedAndCommentByMe = 1
                        else if (likeComment.comment.isNotEmpty())
                            reel.likedAndCommentByMe = 2
                        else
                            reel.likedAndCommentByMe = 0
                    }
                }
                result.first.let { newReels.postValue(it) }
            }
        }

    val reelDetails =
        MutableLiveData<Result<Pair<ArrayList<LikeWithProfile>, ArrayList<CommentWithProfile>>>>()

    fun getCommentsAndLikesByReelsId(reelId: String) = viewModelScope.launch(Dispatchers.IO) {
        reelDetails.postValue(
            Result.success(
                Pair(
                    arrayListOf<LikeWithProfile>(),
                    arrayListOf<CommentWithProfile>()
                )
            )
        )
        homeRepository.getCommentsAndLikesByReelsId(reelId).collectLatest {
            if (it.isSuccess) {
                it.getOrNull()?.let { reel ->
                    val userList =
                        reel.likeAndComment?.keys?.let { it1 -> homeRepository.getUserDataById(it1) }
                    val likeWithProfile = ArrayList<LikeWithProfile>()
                    val commentWithProfile = ArrayList<CommentWithProfile>()
                    reel.likeAndComment?.forEach { map ->
                        val user = userList?.get(map.key)
                        if (map.value.liked) {
                            likeWithProfile.add(
                                LikeWithProfile(
                                    map.key,
                                    user?.name ?: "",
                                    user?.imageUrl ?: "",
                                    map.value.likedTime
                                )
                            )
                        }
                        if (map.value.comment.isNotEmpty()) {
                            commentWithProfile.add(
                                CommentWithProfile(
                                    map.key,
                                    user?.name ?: "",
                                    user?.imageUrl ?: "",
                                    map.value.comment,
                                    map.value.commentTime
                                )
                            )
                        }
                    }
                    reelDetails.postValue(Result.success(Pair(likeWithProfile, commentWithProfile)))
                }
            } else if (it.isFailure) {
                reelDetails.postValue(it.exceptionOrNull()?.let { it1 -> Result.failure(it1) })
            }
        }
    }

    val commentReelStatus = homeRepository.commentReelStatus
    val likeReelStatus = homeRepository.likeReelStatus

    fun likeReel(reelId: String, liked: Boolean = false) = viewModelScope.launch(Dispatchers.IO) {
        homeRepository.likeReel(reelId, liked)
    }

    fun commentReel(reelId: String, comment: String) = viewModelScope.launch(Dispatchers.IO) {
        homeRepository.addCommentToReel(reelId, comment)
    }

    val posts = MutableLiveData<List<Pair<Post, String>>?>()
    val otherUserPosts = MutableLiveData<List<Pair<Post, String>>?>()

    fun getAllPosts(oldList: List<String> = listOf("noList"), userId: String? = null) =
        viewModelScope.launch(Dispatchers.IO) {
            if (userId == null) {
                posts.postValue(null)
            } else {
                otherUserPosts.postValue(null)
            }
            val response = homeRepository.getAllPosts(oldList, userId)
            if (userId == null) {
                posts.postValue(response)
            } else {
                otherUserPosts.postValue(response)
            }
        }

    val searchFragmentPosts = MutableLiveData<List<Pair<Post, String>>?>()

    fun getAllPostsForSearchFragment(
        oldList: List<String> = listOf("noList"),
        searchFragmentPost: Boolean
    ) = viewModelScope.launch(Dispatchers.IO) {
        val response = homeRepository.getAllPosts(oldList, searchFragmentPost)
        searchFragmentPosts.postValue(response)
    }

    val userUploadedReels: LiveData<NetworkResult<List<Reel>>>
        get() = homeRepository.userUploadedReels

    val searchUserUploadedReels: LiveData<NetworkResult<List<Reel>?>>
        get() = homeRepository.searchUserUploadedReels

    fun getUserReels(oldList: List<String> = listOf("noid"), userId: String? = null) =
        viewModelScope.launch(Dispatchers.IO) {
            homeRepository.getUserPostedReels(oldList, userId)
        }

    val profilesFromSearchQuery = homeRepository.profilesFromSearchQuery

    fun getProfilesFromSearch(query: String) = viewModelScope.launch(Dispatchers.IO) {
        homeRepository.getProfileFromSearch(query)
    }

    val call = MutableLiveData<Result<Call>>()
    fun checkVideoCall() = viewModelScope.launch(Dispatchers.IO) {
        homeRepository.checkVideoCall().collectLatest {
            if (it.isSuccess) {
                it.getOrNull()?.let { call -> this@HomeViewModel.call.postValue(Result.success(call)) }
            }
        }
    }

    fun changeCallStatus() = viewModelScope.launch(Dispatchers.IO) {
        homeRepository.changeCallStatus()
    }

}
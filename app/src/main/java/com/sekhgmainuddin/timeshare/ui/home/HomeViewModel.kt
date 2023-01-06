package com.sekhgmainuddin.timeshare.ui.home

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.sekhgmainuddin.timeshare.data.db.entities.PostEntity
import com.sekhgmainuddin.timeshare.data.modals.User
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
) : ViewModel(){

    val firebaseUser= firebaseAuth.currentUser
    val userData= MutableLiveData<User>()

    fun getUserData() = viewModelScope.launch(Dispatchers.IO){
        homeRepository.getUserData().collectLatest {
            if (it.isSuccess){
                userData.postValue(it.getOrNull())
            }
        }
    }

    val addPostStatus= homeRepository.addPostStatus

    fun addPost(list: List<Uri>, description: String)= viewModelScope.launch(Dispatchers.IO){
        homeRepository.addPost(list, description)
    }

    val allPosts= homeRepository.allPosts

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getLatestPosts(friendsList: List<String>)= viewModelScope.launch(Dispatchers.IO) {
        homeRepository.getLatestPosts(friendsList).collectLatest {
            if (it.isSuccess) {
                it.getOrNull()?.let { data ->
                    homeRepository.deleteAllPosts()
                    val userData = homeRepository.getUserDataById(data.second)
                    for (i in data.first) {
                        val temp= firebaseUser?.let { it1 -> i.likeAndComment?.get(it1.uid) }
                        var likeCommentType= 0
                        var comment= ""
                        if(temp!=null){
                            if (temp.comment.isNotEmpty())
                                comment= temp.comment
                            likeCommentType = if (temp.liked && temp.comment.isNotEmpty())
                                3
                            else if (temp.liked)
                                1
                            else if (temp.comment.isNotEmpty())
                                2
                            else
                                0
                        }
                        val user= userData[i.creatorId]
                        val post= PostEntity(i.postId, i.creatorId, i.postDesc,
                            i.postTime,i.postContent,user?.name ?: "",
                            user?.imageUrl ?: "", i.likeCount,
                            i.commentCount, likeCommentType, comment
                        )
                        homeRepository.insertPost(post)
                    }
                }
            }
            if(it.isFailure) {
                Log.d("latestPosts", "getLatestPosts: failure occurred ${it.exceptionOrNull()}")
            }
        }
    }

}
package com.sekhgmainuddin.timeshare.data.modals

import java.io.Serializable


data class Post (

    val postId: String,
    val creatorId: String,
    val postDesc: String,
    val postContent: List<PostImageVideo>? = null,
    val creatorName: String,
    val creatorProfileImage: String,
    val likesCount: Int,
    val commentCount: Int,
    val likedAndCommentByMe: Int

    ) : Serializable
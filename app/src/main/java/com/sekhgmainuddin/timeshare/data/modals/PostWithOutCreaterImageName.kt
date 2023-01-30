package com.sekhgmainuddin.timeshare.data.modals

data class PostWithOutCreaterImageName (
    val postId: String = "",
    val creatorId: String = "",
    val postTime: Long = 0L,
    val postDesc: String = "",
    val postContent: ArrayList<PostImageVideo>? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val likeAndComment: HashMap<String, LikeComment>? = null
)
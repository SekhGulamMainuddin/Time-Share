package com.sekhgmainuddin.timeshare.data.modals

import java.io.Serializable

data class Post (
    var postId: String= "",
    var creatorId: String= "",
    var postDesc: String= "",
    var postContent: MutableList<PostImageVideo>? = null,
    var creatorName: String= "",
    var creatorProfileImage: String= "",
    var postTime: Long= 0L,
    var likeCount: Int= 0,
    var commentCount: Int= 0,
    var likedAndCommentByMe: Int= 0,
    var myComment: String= "",
    var likeAndComment: HashMap<String, LikeComment>? = null

    ) : Serializable
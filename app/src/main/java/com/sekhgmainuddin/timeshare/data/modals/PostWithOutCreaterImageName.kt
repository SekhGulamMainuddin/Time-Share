package com.sekhgmainuddin.timeshare.data.modals

import com.google.firebase.database.PropertyName

data class PostWithOutCreaterImageName (
    @PropertyName("postId") val postId: String,
    @PropertyName("creatorId") val creatorId: String,
    @PropertyName("postTime") val postTime: Long,
    @PropertyName("postDesc") val postDesc: String,
    @PropertyName("postContent") val postContent: ArrayList<PostImageVideo>? = null,
    @PropertyName("likeCount") val likeCount: Int,
    @PropertyName("commentCount") val commentCount: Int,
    @PropertyName("likeAndComment") val likeAndComment: HashMap<String, LikeComment>? = null
) {
    constructor() : this("","",0,"",null,0,0,null)
}
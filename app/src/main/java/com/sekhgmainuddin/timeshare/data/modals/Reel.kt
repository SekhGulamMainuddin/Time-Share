package com.sekhgmainuddin.timeshare.data.modals

data class Reel (
    val reelId: String = "",
    val creatorId: String = "",
    var creatorName: String = "",
    var creatorImageUrl: String = "",
    val reelDesc: String = "",
    val reelPostTime: Long = 0L,
    val reelUrl: String = "",
    val reelViewsCount: Int = 0,
    var reelThumbnail: String = "",
    var likeCount: Int= 0,
    var commentCount: Int= 0,
    var likedAndCommentByMe: Int= 0,
    var myComment: String= "",
    var likeAndComment: HashMap<String, LikeComment>? = null
)
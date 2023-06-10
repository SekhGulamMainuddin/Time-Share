package com.sekhgmainuddin.timeshare.data.modals

data class Call (
    var callId: String = "",
    var token: String = "",
    var uid: Int = 0,
    var callerProfileId: String = "",
    var receiverProfileId: String = "",
    var callerProfileImage: String = "",
    var receiverProfileImage: String = "",
    var callerName: String = "",
    var receiverName: String = "",
    var answered: Boolean = true,
    var typeVideo: Boolean = true,
    var myMicStatus: Boolean = false,
    var oppositeMicStatus: Boolean = false,
    var isGroupCall: Boolean = false
)
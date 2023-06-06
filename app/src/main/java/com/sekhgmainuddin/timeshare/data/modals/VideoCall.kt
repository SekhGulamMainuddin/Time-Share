package com.sekhgmainuddin.timeshare.data.modals

data class VideoCall (
    var callId: String = "",
    var token: String = "",
    var uid: Int = 0,
    var callerProfileId: String = "",
    var receiverProfileId: String = "",
    var answered: Boolean = true,
    var typeVideo: Boolean = true
)
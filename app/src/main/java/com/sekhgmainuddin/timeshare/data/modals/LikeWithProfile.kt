package com.sekhgmainuddin.timeshare.data.modals

data class LikeWithProfile (
    val profileId: String= "",
    val profileName: String= "",
    val profileImage: String= ""
) {
    constructor() : this ("","","")
}
package com.sekhgmainuddin.timeshare.data.modals

import com.google.firebase.database.PropertyName

data class User(
    @PropertyName("name") val name: String,
    @PropertyName("email") val email: String = "",
    @PropertyName("phone") val phone: String = "",
    @PropertyName("imageUrl") val imageUrl: String,
    @PropertyName("bio") val bio: String? = null,
    @PropertyName("interests") val interests: ArrayList<String>? = null,
    @PropertyName("location") val location: String? = null,
    @PropertyName("activeStatus") val activeStatus: Long = 0L,
    @PropertyName("friends") val friends: ArrayList<String>? = null
) {
    constructor() : this ("","","","","",null, "",0L, null)
}
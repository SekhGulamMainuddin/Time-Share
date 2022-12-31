package com.sekhgmainuddin.timeshare.data.modals

data class User(
    val name: String,
    val email: String = "",
    val phone: String = "",
    val imageUrl: String,
    val bio: String? = null,
    val interests: ArrayList<String>? = null,
    val location: String? = null,
    val activeStatus: Long
) {
    constructor() : this ("","","","","",null, "",0)
}
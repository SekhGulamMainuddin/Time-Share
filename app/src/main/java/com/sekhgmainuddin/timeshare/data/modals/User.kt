package com.sekhgmainuddin.timeshare.data.modals

@kotlinx.serialization.Serializable
data class User(
    val name: String = "",
    var userId: String= "",
    val email: String = "",
    val phone: String = "",
    val imageUrl: String = "",
    val bio: String = "",
    val interests: ArrayList<String> = arrayListOf(),
    val location: String = "",
    val activeStatus: Long = 0L,
    val friends: ArrayList<String> = arrayListOf(),
    val followers: ArrayList<String> = arrayListOf(),
    val following: ArrayList<String> = arrayListOf(),
    val isVerified: Boolean = false,
    val status: Status? = null,
    val isSelected: Boolean = false,
    val groups: ArrayList<String> = arrayListOf()
) : java.io.Serializable
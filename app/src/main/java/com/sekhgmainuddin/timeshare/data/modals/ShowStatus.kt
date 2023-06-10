package com.sekhgmainuddin.timeshare.data.modals

import kotlinx.serialization.Serializable

@Serializable
data class ShowStatus(
    val statusList: List<Pair<List<Status>, User>>? = null,
) : java.io.Serializable
package com.sekhgmainuddin.timeshare.utils

sealed class NetworkResult<T>(var data: T? = null, var message: String? = null, var code: Int? = null) {

    class Success<T>(data: T, code: Int?) : NetworkResult<T>(data,null, code)
    class Error<T>(message: String?, data: T? = null, errorCode: Int? = null) : NetworkResult<T>(data, message, errorCode)
    class Loading<T> : NetworkResult<T>()

}
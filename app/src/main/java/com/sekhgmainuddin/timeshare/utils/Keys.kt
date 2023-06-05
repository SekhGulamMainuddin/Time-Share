package com.sekhgmainuddin.timeshare.utils

object Keys {

    init {
        System.loadLibrary("native-lib")
    }

    external fun apiKey(): String
    external fun giphyKey(): String

}
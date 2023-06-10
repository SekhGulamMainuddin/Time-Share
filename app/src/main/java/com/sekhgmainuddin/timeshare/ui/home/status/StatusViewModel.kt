package com.sekhgmainuddin.timeshare.ui.home.status

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatusViewModel @Inject constructor() : ViewModel() {

    val currentIndex= MutableLiveData(0)

}
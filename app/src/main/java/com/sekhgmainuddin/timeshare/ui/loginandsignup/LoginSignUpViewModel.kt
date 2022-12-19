package com.sekhgmainuddin.timeshare.ui.loginandsignup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginSignUpViewModel @Inject constructor(
    private val repository: LoginSignUpRepository
) : ViewModel() {

    val currentUser= repository.currentUser

    val loginResult = repository.result
    fun login(email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.loginEmail(email, password)
    }

    fun googleLogin(firebaseCredential: AuthCredential) = viewModelScope.launch(Dispatchers.IO) {
        repository.googleLogin(firebaseCredential)
    }

}
package com.sekhgmainuddin.timeshare.ui.loginandsignup

import android.graphics.Bitmap
import android.net.Uri
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
        repository.googleLoginOrSignUp(firebaseCredential)
    }

    val signUpResult = repository.signUpResult
    fun signUpEmailPassword(email: String, password: String) = viewModelScope.launch(Dispatchers.IO){
        repository.signUpEmail(email, password)
    }

    val newUserDetailUpload = repository.newUserDetailUpload
    fun uploadNewUserDetail(
        imageUri: Uri?, bitmap: Bitmap?,
        name: String, bio: String, location: String,
        interests: String
    ) = viewModelScope.launch(Dispatchers.IO){
        repository.uploadNewUserDetail(imageUri, bitmap, name, bio, location, interests)
    }

}
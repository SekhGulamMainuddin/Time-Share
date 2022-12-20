package com.sekhgmainuddin.timeshare.ui.loginandsignup

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.utils.NetworkResult
import com.sekhgmainuddin.timeshare.utils.Utils.getFileExtension
import com.sekhgmainuddin.timeshare.utils.Utils.saveAsJPG
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.io.StringBufferInputStream
import javax.inject.Inject

class LoginSignUpRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseStorage: StorageReference,
    @ApplicationContext val context: Context
) {

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    private val _result = MutableLiveData<NetworkResult<FirebaseUser>>()
    val result: LiveData<NetworkResult<FirebaseUser>>
        get() = _result

    suspend fun loginEmail(email: String, password: String) {
        try {
            val response = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Log.d(
                "loginTimeShare",
                "loginEmail: ${response.additionalUserInfo.toString()} ${response.user.toString()}"
            )
            _result.postValue(NetworkResult.Success(response.user!!, 200))
        } catch (firebaseAuthException: FirebaseAuthInvalidUserException) {
            _result.postValue(NetworkResult.Error("User Not Found", errorCode = 404))
        } catch (invalidCredentials: FirebaseAuthInvalidCredentialsException) {
            _result.postValue(NetworkResult.Error("Invalid Credentials", errorCode = 403))
        } catch (e: Exception) {
            Log.d("loginTimeShare", "loginEmail: ${e.localizedMessage} ${e.javaClass}")
            _result.postValue(NetworkResult.Error(e.localizedMessage, errorCode = 500))
        }

    }

    suspend fun googleLoginOrSignUp(firebaseCredential: AuthCredential) {
        try {
            val response = firebaseAuth.signInWithCredential(firebaseCredential).await()
            if (response.additionalUserInfo?.isNewUser == true) {
                val user = response.user
                val newUser = User(
                    user?.displayName ?: "",
                    user?.email ?: "",
                    user?.phoneNumber ?: "",
                    user?.photoUrl.toString()
                )
                user?.uid?.let {
                    firebaseFirestore.collection("Users").document(it).set(newUser).await()
                }
            }
            _result.postValue(NetworkResult.Success(response.user!!, 200))
        } catch (e: Exception) {
            _result.postValue(NetworkResult.Error(e.localizedMessage, errorCode = 500))
        }
    }

    private val _signUpResult = MutableLiveData<NetworkResult<FirebaseUser>>()
    val signUpResult: LiveData<NetworkResult<FirebaseUser>>
        get() = _signUpResult

    suspend fun signUpEmail(email: String, password: String) {
        try {
            val response = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            _signUpResult.postValue(NetworkResult.Success(response.user!!, 201))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            _signUpResult.postValue(
                NetworkResult.Error(
                    "Email or Password is Invalid",
                    errorCode = 403
                )
            )
        } catch (e: Exception) {
            Log.d("signUpEmail", "signUpEmail: $e")
        }
    }

    private val _newUserDetailUpload = MutableLiveData<NetworkResult<String>>()
    val newUserDetailUpload: LiveData<NetworkResult<String>>
        get() = _newUserDetailUpload

    suspend fun uploadNewUserDetail(
        imageUri: Uri?, bitmap: Bitmap?,
        name: String, bio: String, location: String,
        interests: String
    ) {
        val detailMap = HashMap<String, Any>()
        try {
            try {
                var uri: Uri?= imageUri
                if (bitmap!=null){
                    uri = bitmap.saveAsJPG(firebaseAuth.uid.toString(),context) as Uri
                }
                val imageResponse = uri?.let {
                    firebaseStorage.child(
                        "ProfileImage/" + firebaseAuth.uid + "." + getFileExtension(uri,context))
                        .putFile(it).await()
                }
                val uriTask = imageResponse?.storage?.downloadUrl
                while (!uriTask?.isSuccessful!!){}
                val downloadUrl = uriTask?.result
                val download_url = downloadUrl.toString()
                detailMap["imageUrl"] = download_url
            } catch (e: Exception) {
                Log.d("imageUploadException", "uploadNewUserDetail: $e")
            }
            detailMap["name"] = name
            detailMap["bio"] = bio
            detailMap["location"] = location
            detailMap["interests"] = interests
            val response= firebaseAuth.uid?.let { firebaseFirestore.collection("Users").document(it).set(detailMap).await() }
            _newUserDetailUpload.postValue(NetworkResult.Success("Profile Updated Successfully",200))
        } catch (e: Exception) {
            Log.d("uploadNewUser", "uploadNewUserDetail: $e")
            _newUserDetailUpload.postValue(NetworkResult.Error("Some Error Occurred",errorCode = 400))
        }
    }

}
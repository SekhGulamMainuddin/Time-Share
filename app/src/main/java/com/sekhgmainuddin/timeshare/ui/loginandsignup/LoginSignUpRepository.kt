package com.sekhgmainuddin.timeshare.ui.loginandsignup

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.utils.NetworkResult
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.math.log

class LoginSignUpRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseFirestore: FirebaseFirestore
) {

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    private val _result = MutableLiveData<NetworkResult<FirebaseUser>>()
    val result: LiveData<NetworkResult<FirebaseUser>>
        get() = _result

    suspend fun loginEmail(email: String, password: String) {
        try {
            val response = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Log.d("loginTimeShare", "loginEmail: ${response.additionalUserInfo.toString()} ${response.user.toString()}")
            _result.postValue(NetworkResult.Success(response.user!!,200))
        } catch (firebaseAuthException: FirebaseAuthInvalidUserException) {
            _result.postValue(NetworkResult.Error("User Not Found", errorCode = 404))
        } catch (invalidCredentials: FirebaseAuthInvalidCredentialsException){
            _result.postValue(NetworkResult.Error("Invalid Credentials", errorCode = 403))
        }
        catch (e: Exception) {
            Log.d("loginTimeShare", "loginEmail: ${e.localizedMessage} ${e.javaClass}")
            _result.postValue(NetworkResult.Error(e.localizedMessage, errorCode = 500))
        }

    }

    suspend fun googleLogin(firebaseCredential: AuthCredential) {
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
            _result.postValue(NetworkResult.Success(response.user!!,200))
        } catch (e: Exception) {
            _result.postValue(NetworkResult.Error(e.localizedMessage, errorCode = 500))
        }
    }


}
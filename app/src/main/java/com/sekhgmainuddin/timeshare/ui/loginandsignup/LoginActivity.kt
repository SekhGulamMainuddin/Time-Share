package com.sekhgmainuddin.timeshare.ui.loginandsignup

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.IntentSender
import android.graphics.LinearGradient
import android.graphics.Shader
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.MediaController
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.ActivityLoginBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import com.sekhgmainuddin.timeshare.ui.home.MainActivity
import com.sekhgmainuddin.timeshare.utils.NetworkResult
import com.sekhgmainuddin.timeshare.utils.Utils.changeTextColorGradient
import com.sekhgmainuddin.timeshare.utils.Utils.slideVisibility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var oneTapClient: SignInClient
    private lateinit var binding: ActivityLoginBinding
    private lateinit var progressDialog: Dialog
    private lateinit var snackBar: Snackbar
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    @Inject
    lateinit var auth: FirebaseAuth

    private val viewModel by viewModels<LoginSignUpViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        snackBar= Snackbar.make(this,binding.loginLayout," ",Snackbar.LENGTH_SHORT)

        changeTextColorGradient(binding.forgotPassword)
        changeTextColorGradient(binding.signupTV)

        progressDialog = Dialog(this)
        progressDialog.setContentView(R.layout.progress_dialog)
        progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        animateSignInOptions()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        if (viewModel.currentUser != null)
            startActivity(Intent(this, MainActivity::class.java))

        initClickListeners()

        bindObservers()
    }

    private val startForResultOneTapSignIn =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(it.data)
                    val idToken = credential.googleIdToken
                    when {
                        idToken != null -> {
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            viewModel.googleLogin(firebaseCredential)
                        }
                    }
                } catch (e: ApiException) {
                    Toast.makeText(this, "Some Error Occurred", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val startGoogleSignIn =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    viewModel.googleLogin(credential)
                } catch (e: ApiException) {
                    showSnackBar("Failed to Sign In")
                }
            }
        }

    private fun animateSignInOptions() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.textViewOR.slideVisibility(true, 600)
            delay(700)
            binding.googleLogin.slideVisibility(true, 1000)
            delay(1000)
            binding.facebookLogin.slideVisibility(true, 1000)
            delay(1000)
            binding.phoneLogin.slideVisibility(true, 1000)
        }
    }

    private fun initClickListeners() {
        binding.phoneLogin.setOnClickListener {
            startActivity(
                Intent(this, SignUpActivity::class.java).putExtra(
                    "phone",
                    true
                )
            )
            finish()
        }
        binding.continueButton.setOnClickListener {
            if (binding.emailEditText.text.toString().trim().isEmpty())
                binding.emailEditText.error = "Enter the Email"
            else if (!binding.passwordToggleLayout.isVisible)
                binding.passwordToggleLayout.slideVisibility(true, 500)
            else {
                if (binding.passwordEditText.text.toString().isNotEmpty()) {
                    progressDialog.show()
                    viewModel.login(
                        binding.emailEditText.text.toString(),
                        binding.passwordEditText.text.toString()
                    )
                } else
                    binding.passwordEditText.error = "Enter the password"
            }
        }
        binding.googleLogin.setOnClickListener {
            progressDialog.show()
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this) { result ->
                    progressDialog.dismiss()
                    try {
                        startForResultOneTapSignIn.launch(
                            IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                        )

                        Log.d("googleSignIn", "initClickListeners: success")
                    } catch (e: IntentSender.SendIntentException) {
                        Log.d("googleSignIn", "initClickListenersException: ${e.localizedMessage}")
                    }
                }
                .addOnFailureListener(this) { e ->
                    progressDialog.dismiss()
                    Log.d("googleSignIn", "initClickListenersFailed: ${e.message}")
                    showSnackBar(e.localizedMessage ?: "Some Error Occurred")
                    startGoogleSignIn.launch(mGoogleSignInClient.signInIntent)
                }
        }
    }

    private fun bindObservers() {
        viewModel.loginResult.observe(this) {
            progressDialog.dismiss()
            when (it) {
                is NetworkResult.Success -> {
                    if (it.data != null && it.statusCode==200) {
                        showSnackBar("Logged in Successfully")
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    else if(it.data != null && it.statusCode==201){
                        showSnackBar("New User Registered")
                        startActivity(Intent(this, SignUpActivity::class.java)
                            .putExtra("email", it.data!!.email)
                            .putExtra("phone", it.data!!.phoneNumber ?: " "))
                        finish()
                    }
                }
                is NetworkResult.Error -> {
                    when (it.statusCode) {
                        404 -> {
                            startActivity(
                                Intent(
                                    this,
                                    SignUpActivity::class.java
                                ).putExtra("signUpFor", "EmailSignUp")
                            )
                            Toast.makeText(
                                this,
                                "User is not registered. Please Sign Up",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                        403 -> {
                            showSnackBar(it.message!!)
                        }
                        500 -> {
                            showSnackBar(it.message!!)
                        }
                    }
                }
                is NetworkResult.Loading -> {
                    progressDialog.show()
                }
            }
        }
    }

    private fun showSnackBar(message: String) {
        snackBar = Snackbar.make(binding.loginLayout, message, Snackbar.LENGTH_SHORT)
        snackBar.show()
    }

    override fun onResume() {
        super.onResume()

        startLoginVideo()

    }

    private fun startLoginVideo() {
        val uri: Uri = Uri.parse("android.resource://$packageName/${R.raw.test_video}")
        binding.loginVideoAnimation.setVideoURI(uri)
        val mediaController = MediaController(this)
        mediaController.visibility = View.GONE
        binding.loginVideoAnimation.setMediaController(mediaController)
        binding.loginVideoAnimation.requestFocus()
        binding.loginVideoAnimation.start()
        binding.loginVideoAnimation.setOnCompletionListener {
            binding.loginVideoAnimation.start()
        }

    }

}
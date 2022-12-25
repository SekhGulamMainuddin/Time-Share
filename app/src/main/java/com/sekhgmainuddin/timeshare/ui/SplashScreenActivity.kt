package com.sekhgmainuddin.timeshare.ui

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.ActivitySplashScreenBinding
import com.sekhgmainuddin.timeshare.ui.home.MainActivity
import com.sekhgmainuddin.timeshare.ui.loginandsignup.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Glide.with(this).load(R.drawable.time_share_icon).into(binding.splashImage)

        val sharedPreference= getSharedPreferences("time_share_app", MODE_PRIVATE)

        Handler(Looper.getMainLooper()).postDelayed({
            if(sharedPreference.getBoolean("first_launch",true)){
                sharedPreference.edit().putBoolean("first_launch",false).apply()
                startActivity(Intent(this, WelcomeActivity::class.java))
            }
            else if (firebaseAuth.currentUser!=null){
                startActivity(Intent(this, MainActivity::class.java))
            }else{
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        },2000)


    }
}
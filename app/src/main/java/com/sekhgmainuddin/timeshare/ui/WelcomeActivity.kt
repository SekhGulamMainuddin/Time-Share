package com.sekhgmainuddin.timeshare.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sekhgmainuddin.timeshare.databinding.ActivityWelcomeBinding
import com.sekhgmainuddin.timeshare.ui.loginandsignup.LoginActivity
import com.sekhgmainuddin.timeshare.ui.loginandsignup.SignUpActivity

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.getStartedButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.alreadyHATV.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

    }
}
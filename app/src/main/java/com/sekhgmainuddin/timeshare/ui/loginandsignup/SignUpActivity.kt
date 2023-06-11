package com.sekhgmainuddin.timeshare.ui.loginandsignup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.ActivitySignUpBinding
import com.sekhgmainuddin.timeshare.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.sign_up_container_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bundle= Bundle()
        if (intent.getBooleanExtra("phone", false)) {
            bundle.putBoolean("phone", true)
            navController.navigate(R.id.action_signUpFragment_to_phoneOTPFragment, bundle)
        }
        else if (!intent.getStringExtra("email").isNullOrEmpty()){
            bundle.putString("email", intent.getStringExtra("email"))
            bundle.putString("phone", intent.getStringExtra("phone") ?: " ")
            navController.navigate(R.id.action_signUpFragment_to_userNameProfileFragment, bundle)
        }

    }
}
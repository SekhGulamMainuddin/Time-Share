package com.sekhgmainuddin.timeshare.ui.home.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.ActivityListChatBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListChatBinding
    private lateinit var navController: NavController
    private val viewModel by viewModels<ChatsViewModel>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityListChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostController= supportFragmentManager.findFragmentById(R.id.chatFragmentContainer) as NavHostFragment
        navController= navHostController.navController

        viewModel.user.observe(this){

        }

    }

}
package com.sekhgmainuddin.timeshare.ui.home.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.databinding.ActivityChatBinding
import com.sekhgmainuddin.timeshare.ui.home.chat.adapters.ChatsAdapter
import com.sekhgmainuddin.timeshare.utils.NetworkResult
import com.sekhgmainuddin.timeshare.utils.Utils.MSG_TYPE_MESSAGE
import com.sekhgmainuddin.timeshare.utils.Utils.getTimeAgo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val viewModel by viewModels<ChatsViewModel>()
    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var adapter: ChatsAdapter
    private var profileId: String? = null
    private var profileName: String? = null
    private var profileImageUrl: String? = null
    private var profileActiveStatus: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileId= intent.getStringExtra("profileId")
        profileName= intent.getStringExtra("profileName")
        profileImageUrl= intent.getStringExtra("profileImageUrl")

        updateProfileData(User(name = profileName?:"", imageUrl = profileImageUrl?:"", activeStatus = -1))
        loadData()
        registerClickListeners()
        bindObserver()

    }

    fun updateProfileData(user: User){
        if (user.name != profileName) {
            profileName = user.name
            binding.profileName.text= profileName
        }
        if (user.imageUrl != profileImageUrl) {
            profileImageUrl = user.imageUrl
            Glide.with(this).load(profileImageUrl).placeholder(R.drawable.default_profile_pic).into(binding.profileImage)
        }
        if (user.activeStatus != profileActiveStatus) {
            profileActiveStatus = user.activeStatus
            val status= when (profileActiveStatus) {
                -1L, 0L -> ""
                1L -> "Active"
                else -> profileActiveStatus.getTimeAgo()!!
            }
            binding.profileStatus.text= status
            binding.profileStatus.isVisible= status==""
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadData(){

        adapter= ChatsAdapter(this, profileImageUrl!!, firebaseAuth.currentUser?.uid!!)
        val layoutManager= LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        layoutManager.stackFromEnd= true
        binding.chatsRecyclerView.layoutManager= layoutManager
        binding.chatsRecyclerView.adapter= adapter

        viewModel.getProfileDetails(profileId!!)
        viewModel.getLatestChats(profileId!!)

    }

    fun registerClickListeners(){
        binding.backButton.setOnClickListener {
            finish()
        }
        binding.sendMessage.setOnClickListener {
            if (binding.messageInputET.text.toString().isNotEmpty()){
                viewModel.sendMessage(profileId!!, MSG_TYPE_MESSAGE,
                    binding.messageInputET.text.toString(), "")
            }
            else{
                Toast.makeText(this, "Message field is Empty.\nPlease add some message.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun bindObserver(){
        viewModel.sendMessageStatus.observe(this){
            when(it){
                is NetworkResult.Success-> {
                    Toast.makeText(this, "Message Sent", Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Error-> {
                    Toast.makeText(this, "Some Error Occurred", Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Loading-> {

                }
            }
        }
        viewModel.chatList.observe(this){
            adapter.submitList(it)
        }
        viewModel.profileDetail.observe(this){
            updateProfileData(it)
        }
    }

}
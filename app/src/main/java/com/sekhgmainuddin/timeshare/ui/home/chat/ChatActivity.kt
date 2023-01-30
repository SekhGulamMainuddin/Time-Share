package com.sekhgmainuddin.timeshare.ui.home.chat

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
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
import com.sekhgmainuddin.timeshare.utils.Constants.MSG_TYPE_MESSAGE
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
    private lateinit var chatAdapter: ChatsAdapter
    private var profileId: String? = null
    private var profile: User? = null
    private var profileActiveStatus: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.getBundleExtra("profileBundle")
        profileId = bundle?.getString("profileId")
        profile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            bundle?.getSerializable("profile", User::class.java)
        else
            bundle?.getSerializable("profile") as User

        updateProfileData(
            User(
                name = profile?.name ?: "",
                imageUrl = profile?.imageUrl ?: "",
                activeStatus = profile?.activeStatus ?: -1
            )
        )
        loadData()
        registerClickListeners()
        bindObserver()

    }

    fun updateProfileData(user: User) {
        binding.apply {
            profileName.text = profile?.name?:""
            Glide.with(this@ChatActivity).load(profile?.imageUrl?:"").placeholder(R.drawable.default_profile_pic)
                .into(profileImage)
            if (user.activeStatus != profileActiveStatus) {
                profileActiveStatus = user.activeStatus
                val status = when (profileActiveStatus) {
                    -1L, 0L -> ""
                    1L -> "Active"
                    else -> profileActiveStatus.getTimeAgo()!!
                }
                profileStatus.text = status
                profileStatus.isVisible = status == ""
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadData() {

        chatAdapter = ChatsAdapter(this, profile?.imageUrl ?: "", firebaseAuth.currentUser?.uid!!)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        layoutManager.stackFromEnd = true
        binding.chatsRecyclerView.layoutManager = layoutManager
        binding.chatsRecyclerView.adapter = chatAdapter

        viewModel.getLatestChats(profileId!!)

    }

    fun registerClickListeners() {
        binding.apply {
            backButton.setOnClickListener {
                finish()
            }
            sendMessage.setOnClickListener {
                if (messageInputET.text.toString().isNotEmpty()) {
                    viewModel.sendMessage(
                        profileId!!, MSG_TYPE_MESSAGE,
                        messageInputET.text.toString(), ""
                    )
                    messageInputET.text.clear()
                } else {
                    Toast.makeText(
                        this@ChatActivity,
                        "Message field is Empty.\nPlease add some message.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun bindObserver() {
        viewModel.sendMessageStatus.observe(this) {
            when (it) {
                is NetworkResult.Success -> {
                    Toast.makeText(this, "Message Sent", Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Error -> {
                    Toast.makeText(this, "Some Error Occurred", Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Loading -> {

                }
            }
        }
        viewModel.chatList.observe(this) {
            chatAdapter.submitList(it)
        }
    }

}
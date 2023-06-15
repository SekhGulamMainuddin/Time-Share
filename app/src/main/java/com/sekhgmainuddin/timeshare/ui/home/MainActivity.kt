package com.sekhgmainuddin.timeshare.ui.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.ActivityMainBinding
import com.sekhgmainuddin.timeshare.ui.home.addnewpostreelorstatus.fragments.AddNewPostReelStatusBottomSheetDialogFragment
import com.sekhgmainuddin.timeshare.ui.home.chat.backend.ChatsViewModel
import com.sekhgmainuddin.timeshare.ui.home.chat.ui.groupchat.GroupCallActivity
import com.sekhgmainuddin.timeshare.ui.home.chat.ui.singlechat.CallActivity
import com.sekhgmainuddin.timeshare.ui.home.newuserfollow.NewUserFollowDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var addNewPostBottomSheetDialogFragment: AddNewPostReelStatusBottomSheetDialogFragment
    private lateinit var newUserFollowDialog: NewUserFollowDialog
    private val viewModel by viewModels<HomeViewModel>()
    private val chatsViewModel by viewModels<ChatsViewModel>()

    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userId = firebaseAuth.currentUser?.uid
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.mainScreenFragmentContainer) as NavHostFragment
        navController = navHostFragment.navController

        addNewPostBottomSheetDialogFragment = AddNewPostReelStatusBottomSheetDialogFragment{
            when(it){
                0->{
                    navController.navigate(R.id.addStatusFragment)
                }
                1->{
                    navController.navigate(R.id.fragmentAddPost)
                }
                2->{
                    navController.navigate(R.id.fragmentAddReel)
                }
            }
        }
        newUserFollowDialog= NewUserFollowDialog()

        setUpBottomNavigationBar()
        bindObserver()
        viewModel.checkVideoCall()

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            viewModel.updateToken(task.result)
        })

        if (intent.getBooleanExtra("isNewUser", false)){
            Toast.makeText(this, "Follow some People to See Posts", Toast.LENGTH_SHORT).show()
            newUserFollowDialog.show(supportFragmentManager, "FollowPeople")
        }

    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun bindObserver() {
        viewModel.call.observe(this) {
            it.onSuccess { call ->
                if (call.receiverProfileId == userId && !call.answered) {
                    startActivity(
                        Intent(this, if (call.isGroupCall) GroupCallActivity::class.java else CallActivity::class.java)
                            .putExtra("agoraToken", call.token)
                            .putExtra("profileId", call.callerProfileId)
                            .putExtra("byMe", false)
                            .putExtra("callId", call.callId)
                            .putExtra("typeVideo", call.typeVideo)
                            .putExtra("profileImage", call.callerProfileImage)
                            .putExtra("profileName", call.callerName)
                    )
                    viewModel.changeCallStatus()
                }
            }
        }
        chatsViewModel.user.observe(this){
            if (it.isNotEmpty()) {
                chatsViewModel.friendsList.clear()
                chatsViewModel.friendsList.putAll(it[0].friends)
            }
        }
    }

    private fun setUpBottomNavigationBar() {
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    navController.navigate(R.id.homeScreenFragment)
                }

                R.id.search -> {
                    navController.navigate(R.id.searchFragment)
                }

                R.id.addPost -> {
                    addNewPostBottomSheetDialogFragment.show(supportFragmentManager, "AddNewPost")
                    return@setOnItemSelectedListener false
                }

                R.id.reels -> {
                    navController.navigate(R.id.reelsFragment)
                }

                R.id.profile -> {
                    navController.navigate(R.id.myProfileFragment)
                }
            }

            true
        }
    }

    override fun onBackPressed() {
        navController.popBackStack()
        when(navController.currentBackStackEntry?.id){

        }
//        super.onBackPressed()
    }

}
package com.sekhgmainuddin.timeshare.ui.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
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
    private lateinit var navHostFragment: NavHostFragment
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
        navHostFragment =
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
                chatsViewModel.followingList.clear()
                chatsViewModel.followingList.putAll(it[0].following)
                Log.d("friendandfollowingList", "bindObserver: ${it[0].friends}")
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

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                navController.popBackStack()
                if (navHostFragment.childFragmentManager.backStackEntryCount==0)
                    finish()
                when(navController.currentDestination?.id){
                    R.id.homeScreenFragment, R.id.postDetailFragment, R.id.profileFragment, R.id.fragmentAddPost, R.id.addStatusFragment, R.id.fragmentAddReel -> {
                        checkBottomBarItem(R.id.home)
                    }
                    R.id.searchFragment-> {
                        checkBottomBarItem(R.id.search)
                    }
                    R.id.reelsFragment-> {
                        checkBottomBarItem(R.id.reels)
                    }
                    R.id.myProfileFragment-> {
                        checkBottomBarItem(R.id.profile)
                    }
                    else-> {
                        binding.bottomNavigation.isVisible= false
                    }
                }
            }
        })

    }

    fun checkBottomBarItem(id: Int) {
        binding.bottomNavigation.menu.findItem(id).isChecked = true
        binding.bottomNavigation.isVisible= true
    }

}
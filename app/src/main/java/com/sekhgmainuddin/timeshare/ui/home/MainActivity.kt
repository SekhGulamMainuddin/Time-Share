package com.sekhgmainuddin.timeshare.ui.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.mbms.GroupCall
import android.widget.Toast
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.ActivityMainBinding
import com.sekhgmainuddin.timeshare.ui.home.addnewpostreelorstatus.fragments.AddNewPostReelStatusBottomSheetDialogFragment
import com.sekhgmainuddin.timeshare.ui.home.chat.ui.groupchat.GroupCallActivity
import com.sekhgmainuddin.timeshare.ui.home.chat.ui.singlechat.CallActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var addNewPostBottomSheetDialogFragment: AddNewPostReelStatusBottomSheetDialogFragment
    private val viewModel by viewModels<HomeViewModel>()

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

        addNewPostBottomSheetDialogFragment = AddNewPostReelStatusBottomSheetDialogFragment()

        setUpBottomNavigationBar()
        bindObserver()
        viewModel.checkVideoCall()
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun bindObserver() {
        viewModel.call.observe(this) {
            it.onSuccess { call ->
                Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
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
//        when(navController.currentBackStackEntry?.id){
//
//        }
//        super.onBackPressed()
    }


}
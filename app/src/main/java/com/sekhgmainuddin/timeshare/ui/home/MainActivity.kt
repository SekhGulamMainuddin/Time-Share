package com.sekhgmainuddin.timeshare.ui.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.modals.PostImageVideo
import com.sekhgmainuddin.timeshare.databinding.ActivityMainBinding
import com.sekhgmainuddin.timeshare.ui.home.addnewpostorreel.AddNewPostOrReelActivity
import com.sekhgmainuddin.timeshare.ui.home.reels.ReelsActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private val viewModel by viewModels<HomeViewModel>()
    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    @Inject
    lateinit var firebaseDatabase: DatabaseReference
    private var currSelectedPage: Int= R.id.home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val firebaseUser= firebaseAuth.currentUser

        val navHostFragment= supportFragmentManager.findFragmentById(R.id.mainScreenFragmentContainer) as NavHostFragment
        navController= navHostFragment.navController

        setUpBottomNavigationBar()
        setUpAddPostBottomSheetDialog()

    }

    private fun setUpAddPostBottomSheetDialog() {
        bottomSheetDialog= BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_add_post_dialog)
        bottomSheetDialog.findViewById<ImageButton>(R.id.addPostButton)?.setOnClickListener {
            startActivity(Intent(this, AddNewPostOrReelActivity::class.java).putExtra("type",0))
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.findViewById<ImageButton>(R.id.addReelsButton)?.setOnClickListener {
            startActivity(Intent(this, AddNewPostOrReelActivity::class.java).putExtra("type",1))
            bottomSheetDialog.dismiss()
        }
    }

    private fun setUpBottomNavigationBar() {
        binding.bottomNavigation.menu.findItem(R.id.addPost).isCheckable = false
        binding.bottomNavigation.menu.findItem(R.id.reels).isCheckable = false
        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home-> {
                    currSelectedPage= R.id.home
                    navController.navigate(R.id.homeScreenFragment)
                }
                R.id.search-> {
                    currSelectedPage= R.id.search
                    navController.navigate(R.id.searchFragment)
                }
                R.id.addPost-> {
                    binding.bottomNavigation.menu.findItem(currSelectedPage).isChecked= true
                    bottomSheetDialog.show()
                }
                R.id.reels-> {
                    binding.bottomNavigation.menu.findItem(currSelectedPage).isChecked= true
                    startActivity(Intent(this, ReelsActivity::class.java))
                }
                R.id.profile-> {
                    currSelectedPage= R.id.profile
                    navController.navigate(R.id.myProfileFragment)
                }
            }

            true
        }
    }


}
package com.sekhgmainuddin.timeshare.ui.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.ActivityMainBinding
import com.sekhgmainuddin.timeshare.ui.home.addnewpostreelorstatus.fragments.AddNewPostReelStatusBottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var addNewPostBottomSheetDialogFragment: AddNewPostReelStatusBottomSheetDialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment= supportFragmentManager.findFragmentById(R.id.mainScreenFragmentContainer) as NavHostFragment
        navController= navHostFragment.navController

        addNewPostBottomSheetDialogFragment= AddNewPostReelStatusBottomSheetDialogFragment()

        setUpBottomNavigationBar()

    }

    private fun setUpBottomNavigationBar() {
        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home-> {
                    navController.navigate(R.id.homeScreenFragment)
                }
                R.id.search-> {
                    navController.navigate(R.id.searchFragment)
                }
                R.id.addPost-> {
                    addNewPostBottomSheetDialogFragment.show(supportFragmentManager, "AddNewPost")
                }
                R.id.reels-> {
                    navController.navigate(R.id.reelsFragment)
                }
                R.id.profile-> {
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
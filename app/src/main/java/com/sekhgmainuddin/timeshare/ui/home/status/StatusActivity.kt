package com.sekhgmainuddin.timeshare.ui.home.status

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.sekhgmainuddin.timeshare.data.modals.ShowStatus
import com.sekhgmainuddin.timeshare.data.modals.Status
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.databinding.ActivityStatusBinding
import com.sekhgmainuddin.timeshare.ui.home.status.adapters.StatusPagerAdapter
import com.sekhgmainuddin.timeshare.ui.home.status.fragments.StatusFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class StatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatusBinding
    private lateinit var statusPagerAdapter: StatusPagerAdapter
    private val viewModel by viewModels<StatusViewModel>()
    private var status: ShowStatus? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        status= if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getSerializableExtra("searchUser", ShowStatus::class.java)
        } else {
            intent?.getSerializableExtra("searchUser") as ShowStatus
        }

        val statusList= status?.statusList?.map {
            StatusFragment(it.first, it.second)
        }

        statusPagerAdapter = StatusPagerAdapter(
            statusList!!,
            supportFragmentManager,
            lifecycle
        )
        binding.statusViewPager.adapter = statusPagerAdapter

        viewModel.currentIndex.observe(this) {
            if (it == status?.statusList?.size) {
                finish()
            } else {
                binding.statusViewPager.currentItem = it
            }
        }

    }


}
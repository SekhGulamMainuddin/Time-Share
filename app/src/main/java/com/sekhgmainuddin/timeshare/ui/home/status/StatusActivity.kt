package com.sekhgmainuddin.timeshare.ui.home.status

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.sekhgmainuddin.timeshare.data.modals.Status
import com.sekhgmainuddin.timeshare.data.modals.User
import com.sekhgmainuddin.timeshare.databinding.ActivityStatusBinding
import com.sekhgmainuddin.timeshare.ui.home.status.adapters.StatusPagerAdapter
import com.sekhgmainuddin.timeshare.ui.home.status.fragments.StatusFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class StatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatusBinding
    private lateinit var statusPagerAdapter: StatusPagerAdapter
    private val viewModel by viewModels<StatusViewModel>()
    private var status: ArrayList<Pair<List<Status>, User>>? = null
    private var startIndex= 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startIndex= intent?.getIntExtra("startIndex", 0)!!
        status= Json.decodeFromString<ArrayList<Pair<List<Status>, User>>>(intent?.getStringExtra("statusData")!!)

        val statusList= status?.map {
            StatusFragment(it.first, it.second)
        }

        statusPagerAdapter = StatusPagerAdapter(
            statusList!!,
            supportFragmentManager,
            lifecycle
        )
        binding.statusViewPager.adapter = statusPagerAdapter

        viewModel.currentIndex.postValue(startIndex)
        viewModel.currentIndex.observe(this) {
            if (it == status?.size) {
                finish()
            } else {
                binding.statusViewPager.currentItem = it
            }
        }

    }


}
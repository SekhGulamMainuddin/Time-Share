package com.sekhgmainuddin.timeshare.ui.home.status.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sekhgmainuddin.timeshare.ui.home.status.fragments.StatusFragment

class StatusPagerAdapter(val statusFragmentList: List<StatusFragment>,fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return statusFragmentList[position]
    }

    override fun getItemCount(): Int {
        return statusFragmentList.size
    }


}
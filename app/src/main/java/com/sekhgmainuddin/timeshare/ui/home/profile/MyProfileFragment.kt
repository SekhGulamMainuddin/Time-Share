package com.sekhgmainuddin.timeshare.ui.home.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.tabs.TabLayoutMediator
import com.sekhgmainuddin.timeshare.databinding.FragmentMyProfileBinding
import com.sekhgmainuddin.timeshare.ui.home.profile.adapters.ProfileViewPagerAdapter
import com.sekhgmainuddin.timeshare.ui.home.profile.fragments.PostsFragment
import com.sekhgmainuddin.timeshare.ui.home.profile.fragments.ReelsFragment

class MyProfileFragment : Fragment() {

    private var _binding: FragmentMyProfileBinding? = null
    private val binding: FragmentMyProfileBinding
        get() = _binding!!
    private lateinit var viewPagerAdapter: ProfileViewPagerAdapter
    private val fragmentNames = listOf("Posts", "Reels")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyProfileBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPagerAdapter = ProfileViewPagerAdapter(
            listOf(PostsFragment(), ReelsFragment()),
            childFragmentManager,
            lifecycle
        )

        binding.viewPager2.adapter = viewPagerAdapter

        TabLayoutMediator(
            binding.profileTabs,
            binding.viewPager2
        ) { tab, position ->
            tab.text = fragmentNames[position]
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
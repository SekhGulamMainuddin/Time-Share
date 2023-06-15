package com.sekhgmainuddin.timeshare.ui.home.chat.ui.chatlist.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.FragmentChatsListBinding
import com.sekhgmainuddin.timeshare.ui.home.chat.ui.adapters.ChatsListViewPagerAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatsListFragment : Fragment() {

    private var _binding: FragmentChatsListBinding? = null
    private val binding: FragmentChatsListBinding
        get() = _binding!!
    private lateinit var chatsListViewPagerAdapter: ChatsListViewPagerAdapter
    private val tabNames= listOf(
        "Recent",
        "Call",
        "Friends"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsListBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
        registerListeners()

    }

    private fun initialize() {
        binding.apply {
            chatsListViewPagerAdapter= ChatsListViewPagerAdapter(listOf(
                RecentChatsFragment(),
                CallsFragment(),
                FriendsListFragment()
            ), childFragmentManager, lifecycle)

            chatsListViewPager.adapter= chatsListViewPagerAdapter
            TabLayoutMediator(
                tabLayout,
                chatsListViewPager,
                TabLayoutMediator.TabConfigurationStrategy{ tab, position ->
                    tab.text= tabNames[position]
                }
            ).attach()
        }

    }

    private fun registerListeners() {
        binding.apply {
            messageTo.setOnClickListener {
                binding.chatsListViewPager.currentItem= 2
            }
            addGroup.setOnClickListener {
                findNavController().navigate(R.id.action_chatsListFragment2_to_addGroupFragment)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
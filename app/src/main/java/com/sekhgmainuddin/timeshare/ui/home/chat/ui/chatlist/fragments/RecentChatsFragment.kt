package com.sekhgmainuddin.timeshare.ui.home.chat.ui.chatlist.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.db.entities.GroupEntity
import com.sekhgmainuddin.timeshare.databinding.FragmentRecentChatsBinding
import com.sekhgmainuddin.timeshare.ui.home.chat.backend.ChatsViewModel
import com.sekhgmainuddin.timeshare.ui.home.chat.ui.adapters.ChatsListAdapter
import com.sekhgmainuddin.timeshare.ui.home.chat.ui.adapters.onClicked
import kotlinx.coroutines.ExperimentalCoroutinesApi

class RecentChatsFragment : Fragment(), onClicked {

    private var _binding: FragmentRecentChatsBinding?= null
    private val binding: FragmentRecentChatsBinding
        get() = _binding!!
    private val viewModel by activityViewModels<ChatsViewModel>()
    private lateinit var adapter: ChatsListAdapter
    val groupMapId = hashMapOf<String, GroupEntity>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentRecentChatsBinding.inflate(inflater)
        return _binding!!.root
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
        viewModel.getRecentProfileChats()
        bindObservers()
    }

    private fun initialize() {
        adapter = ChatsListAdapter(requireContext(), this)
        binding.apply {
            chatsListRV.adapter = adapter
        }
    }

    fun bindObservers() {
        viewModel.recentChatProfiles.observe(viewLifecycleOwner) {
            if (adapter.currentList.size == 1 && it.size > 1) {
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
            } else {
                adapter.submitList(it)
            }
        }
        viewModel.groups.observe(viewLifecycleOwner) {
            it.forEach { group->
                groupMapId[group.groupId] = group
            }
        }
    }

    override fun onProfileClicked(profileId: String, profileName: String, profileImageUrl: String, isGroup: Boolean) {
        if (isGroup){
            val group= groupMapId[profileId]
            if (group!=null){
                val bundle = Bundle()
                bundle.putSerializable("profile", group)
                bundle.putString("profileId", profileId)
                bundle.putBoolean("isGroup", true)
                findNavController().navigate(R.id.action_chatsListFragment2_to_chatsFragment, args = bundle)
            } else {
                Toast.makeText(requireContext(), "Some Error Occurred while fetching group details", Toast.LENGTH_SHORT).show()
            }
        } else {
            viewModel.friendsList[profileId]?.let {
                val bundle = Bundle()
                bundle.putSerializable("profile", it)
                bundle.putString("profileId", profileId)
                bundle.putBoolean("isGroup",false)
                findNavController().navigate(R.id.action_chatsListFragment2_to_chatsFragment, args = bundle)
            }
            if (!viewModel.friendsList.containsKey(profileId)) {
                Toast.makeText(requireContext(), "Not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }


}
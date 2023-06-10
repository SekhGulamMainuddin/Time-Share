package com.sekhgmainuddin.timeshare.ui.home.chat.ui.chatlist.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.data.db.entities.GroupEntity
import com.sekhgmainuddin.timeshare.databinding.FragmentChatsListBinding
import com.sekhgmainuddin.timeshare.ui.home.chat.ui.ChatActivity
import com.sekhgmainuddin.timeshare.ui.home.chat.backend.ChatsViewModel
import com.sekhgmainuddin.timeshare.ui.home.chat.ui.adapters.ChatsListAdapter
import com.sekhgmainuddin.timeshare.ui.home.chat.ui.adapters.onClicked
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class ChatsListFragment : Fragment(), onClicked {

    private var _binding: FragmentChatsListBinding? = null
    private val binding: FragmentChatsListBinding
        get() = _binding!!
    private val viewModel by activityViewModels<ChatsViewModel>()
    private lateinit var adapter: ChatsListAdapter
    val groupMapId = hashMapOf<String, GroupEntity>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsListBinding.inflate(inflater)
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
            messageTo.setOnClickListener {
                findNavController().navigate(R.id.action_chatsListFragment_to_friendsListFragment)
            }
            addGroup.setOnClickListener {
                findNavController().navigate(R.id.action_chatsListFragment_to_addGroupFragment)
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onProfileClicked(profileId: String, profileName: String, profileImageUrl: String, isGroup: Boolean) {
        if (isGroup){
            val group= groupMapId[profileId]
            if (group!=null){
                val bundle = Bundle()
                bundle.putSerializable("profile", group)
                bundle.putString("profileId", profileId)
                bundle.putBoolean("isGroup", true)
                startActivity(
                    Intent(requireContext(), ChatActivity::class.java)
                        .putExtra("profileBundle", bundle)
                )
            } else {
                Toast.makeText(requireContext(), "Some Error Occurred while fetching group details", Toast.LENGTH_SHORT).show()
            }
        } else {
            viewModel.user.value?.get(0)?.friends?.get(profileId)?.let {
                val bundle = Bundle()
                bundle.putSerializable("profile", it)
                bundle.putString("profileId", profileId)
                bundle.putBoolean("isGroup",false)
                startActivity(
                    Intent(requireContext(), ChatActivity::class.java)
                        .putExtra("profileBundle", bundle)
                )
            }
        }
    }

}
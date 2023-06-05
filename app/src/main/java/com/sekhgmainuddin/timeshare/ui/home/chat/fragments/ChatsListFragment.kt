package com.sekhgmainuddin.timeshare.ui.home.chat.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sekhgmainuddin.timeshare.R
import com.sekhgmainuddin.timeshare.databinding.FragmentChatsListBinding
import com.sekhgmainuddin.timeshare.ui.home.chat.ChatActivity
import com.sekhgmainuddin.timeshare.ui.home.chat.ChatsViewModel
import com.sekhgmainuddin.timeshare.ui.home.chat.adapters.ChatsListAdapter
import com.sekhgmainuddin.timeshare.ui.home.chat.adapters.onClicked
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class ChatsListFragment : Fragment(), onClicked {

    private var _binding: FragmentChatsListBinding? = null
    private val binding: FragmentChatsListBinding
        get() = _binding!!
    private val viewModel by activityViewModels<ChatsViewModel>()
    private lateinit var adapter: ChatsListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentChatsListBinding.inflate(inflater)
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
        adapter= ChatsListAdapter(requireContext(),this)
        binding.apply{
            chatsListRV.adapter = adapter
            messageTo.setOnClickListener {
                findNavController().navigate(R.id.action_chatsListFragment_to_friendsListFragment)
            }
        }
    }

    fun bindObservers(){
        viewModel.recentChatProfiles.observe(viewLifecycleOwner){
            if (adapter.currentList.size==1 && it.size>1){
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
            }else{
                adapter.submitList(it)
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }

    override fun onProfileClicked(profileId: String, profileName: String, profileImageUrl: String) {
        viewModel.user.value?.get(0)?.friends?.get(profileId)?.let{
            val bundle= Bundle()
            bundle.putSerializable("profile", it)
            bundle.putString("profileId", profileId)
            startActivity(Intent(requireContext(), ChatActivity::class.java)
                .putExtra("profileBundle", bundle)
            )
        }
    }

}
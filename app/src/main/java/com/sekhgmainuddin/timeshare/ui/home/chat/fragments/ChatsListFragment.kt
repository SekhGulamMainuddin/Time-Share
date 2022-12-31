package com.sekhgmainuddin.timeshare.ui.home.chat.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
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

        adapter= ChatsListAdapter(requireContext(),this)
        binding.chatsListRV.adapter= adapter

        viewModel.getRecentProfileChats()

//        findNavController().navigate(R.id.action_chatsListFragment_to_chatFragment)

    /*    binding.chatsListRV.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    //You should HIDE filter view here
                } else if (dy < 0) {
                    System.out.println("Scrolled Upwards");
                    if ((binding.chatsListRV.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() == 0) {
                        //this is the top of the RecyclerView
                        System.out.println("==================================== >>>> Detect top of the item List");
                        //You should visible filter view here
                    } else {
                        //You should HIDE filter view here
                    }
                } else {
                    System.out.println("No Vertical Scrolled");
                    //You should HIDE filter view here
                }
            }
        })*/

        bindObservers()

    }

    fun bindObservers(){
        viewModel.recentChatProfiles.observe(viewLifecycleOwner){
            adapter.submitList(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }

    override fun onProfileClicked(profileId: String, profileName: String, profileImageUrl: String) {
        startActivity(Intent(requireContext(), ChatActivity::class.java)
            .putExtra("profileId", profileId)
            .putExtra("profileName", profileName)
            .putExtra("profileImageUrl", profileImageUrl)
        )
    }

}
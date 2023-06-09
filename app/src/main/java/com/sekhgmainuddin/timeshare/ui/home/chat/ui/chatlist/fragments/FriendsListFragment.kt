package com.sekhgmainuddin.timeshare.ui.home.chat.ui.chatlist.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sekhgmainuddin.timeshare.databinding.FragmentFriendsListBinding
import com.sekhgmainuddin.timeshare.ui.home.chat.ui.oneononechat.ChatActivity
import com.sekhgmainuddin.timeshare.ui.home.chat.backend.ChatsViewModel
import com.sekhgmainuddin.timeshare.ui.home.chat.backend.adapters.FriendsListAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendsListFragment : Fragment() {

    private var _binding: FragmentFriendsListBinding? = null
    private val binding: FragmentFriendsListBinding
        get() = _binding!!
    private val viewModel by activityViewModels<ChatsViewModel>()
    private lateinit var friendAdapter: FriendsListAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendsListBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
        bindObservers()
    }

    private fun initialize() {
        friendAdapter = FriendsListAdapter(requireContext()) {
            val bundle = Bundle()
            bundle.putString("profileId", it.first)
            bundle.putSerializable("profile", it.second)
            startActivity(
                Intent(requireContext(), ChatActivity::class.java)
                    .putExtra("profileBundle", bundle)
            )
        }
        binding.apply {
            friendsRecyclerView.adapter = friendAdapter
        }
    }

    private fun bindObservers() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user[0].friends.let {
                friendAdapter.submitList(it.toList())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
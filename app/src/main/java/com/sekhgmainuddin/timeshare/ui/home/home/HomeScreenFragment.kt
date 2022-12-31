package com.sekhgmainuddin.timeshare.ui.home.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sekhgmainuddin.timeshare.databinding.FragmentHomeScreenBinding
import com.sekhgmainuddin.timeshare.ui.home.chat.ChatListActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeScreenFragment : Fragment() {

    private var _binding: FragmentHomeScreenBinding? = null
    private val binding: FragmentHomeScreenBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentHomeScreenBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerListeners()
    }

    fun registerListeners(){
        binding.messages.setOnClickListener {
            startActivity(Intent(requireContext(), ChatListActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }

}
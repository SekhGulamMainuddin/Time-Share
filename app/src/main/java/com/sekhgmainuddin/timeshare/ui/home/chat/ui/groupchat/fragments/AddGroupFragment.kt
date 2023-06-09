package com.sekhgmainuddin.timeshare.ui.home.chat.ui.groupchat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sekhgmainuddin.timeshare.databinding.FragmentAddGroupBinding

class AddGroupFragment : Fragment(){

    private var _binding: FragmentAddGroupBinding?= null
    private val binding: FragmentAddGroupBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentAddGroupBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }


}